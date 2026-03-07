package net.andrecarbajal.naviMusic.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.andrecarbajal.naviMusic.audio.resultHandler.AudioResultHandler
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyPlaylistResultHandler
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyTrackResultHandler
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyFetch
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyPlaylist
import net.andrecarbajal.naviMusic.audio.spotify.SpotifySong
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@Service
class MusicService(@Lazy private val audioPlayerManager: AudioPlayerManager, private val spotifyFetch: SpotifyFetch) {

    private val log = LoggerFactory.getLogger(MusicService::class.java)
    private val managers = mutableMapOf<Long, GuildMusicManager>()

    fun getGuildMusicManager(guild: Guild): GuildMusicManager {
        val musicManager = managers.getOrPut(guild.idLong) {
            GuildMusicManager(audioPlayerManager, guild)
        }
        guild.audioManager.sendingHandler = musicManager.getSendHandler()
        return musicManager
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadAndPlay(
        channel: TextChannel,
        provider: String,
        track: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        loadItem(channel.guild, member, "$provider: $track", AudioResultHandler(this, channel.guild, member!!, event))
        return RichResponse("Loading track...", RichResponse.Type.OK, false)
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadAndPlayUrl(
        channel: TextChannel, trackUrl: String, member: Member?, event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        loadItem(channel.guild, member, trackUrl, AudioResultHandler(this, channel.guild, member!!, event))
        return RichResponse("Loading track from URL...", RichResponse.Type.OK, false)
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadAndPlaySpotifyUrl(
        channel: TextChannel,
        provider: String,
        trackUrl: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        // Iniciamos la conexión en paralelo
        val connectionFuture = connectToChannel(channel.guild.audioManager, member, true)

        // Iniciamos el fetch de Spotify en un hilo virtual para no bloquear
        Thread.ofVirtual().start {
            try {
                val isTrack = trackUrl.contains("track", ignoreCase = true)
                val isPlaylist = trackUrl.contains("playlist", ignoreCase = true)
                val isAlbum = trackUrl.contains("album", ignoreCase = true)

                if (isTrack) {
                    val song = spotifyFetch.fetchSong(trackUrl) ?: return@start
                    connectionFuture.thenAccept { connected ->
                        if (connected) {
                            val spotifyTrackResultHandler =
                                SpotifyTrackResultHandler(this, channel.guild, member, song, event)
                            audioPlayerManager.loadItemOrdered(
                                getGuildMusicManager(channel.guild), "$provider: $song", spotifyTrackResultHandler
                            )
                        }
                    }
                } else if (isPlaylist || isAlbum) {
                    val playlist =
                        if (isPlaylist) spotifyFetch.fetchPlaylist(trackUrl) else spotifyFetch.fetchAlbum(trackUrl)
                    if (playlist != null) {
                        connectionFuture.thenAccept { connected ->
                            if (connected) {
                                loadSpotifySongs(playlist, channel, member, provider)
                                event?.let {
                                    spotifyResponse(
                                        channel.guild,
                                        member,
                                        playlist,
                                        if (isPlaylist) "playlist" else "album",
                                        trackUrl
                                    ).editReply(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Error processing Spotify URL", e)
            }
        }

        return RichResponse("Processing Spotify link...", RichResponse.Type.OK, false)
    }

    private fun loadItem(guild: Guild, member: Member?, identifier: String, handler: AudioLoadResultHandler) {
        val musicManager = getGuildMusicManager(guild)
        connectToChannel(guild.audioManager, member, true).thenAccept { connected ->
            if (connected) {
                audioPlayerManager.loadItemOrdered(musicManager, identifier, handler)
            }
        }
    }

    private fun spotifyResponse(
        guild: Guild, member: Member?, playlist: SpotifyPlaylist, type: String, url: String = ""
    ): RichResponse {
        val playlistSize = playlist.songs.size
        val sizeInQueue = getGuildMusicManager(guild).scheduler.getQueueSize() + playlistSize

        val response = RichResponse(
            title = "Adding Spotify $type to queue", text = "[${playlist.title}]($url)", fields = listOf(
                MessageEmbed.Field("Songs", "$playlistSize songs", true),
                MessageEmbed.Field("In queue", if (sizeInQueue == 1) "1 song" else "$sizeInQueue songs", true)
            ), footer = member?.let {
                RichResponse.Footer(text = "Added by ${it.effectiveName}", imageUrl = it.effectiveAvatarUrl)
            })
        return response
    }

    private fun loadSpotifySongs(playlist: SpotifyPlaylist, channel: TextChannel, member: Member?, provider: String) {
        playlist.songs.forEach { song: SpotifySong ->
            log.warn("Loading song: {}", song.toString())
            val musicManager = getGuildMusicManager(channel.guild)
            val spotifyPlaylistResultHandler = SpotifyPlaylistResultHandler(this, channel.guild, member)
            audioPlayerManager.loadItemOrdered(musicManager, "$provider: $song", spotifyPlaylistResultHandler)
        }
    }

    fun skipTrack(channel: TextChannel, position: Int): RichResponse {
        val musicManager = getGuildMusicManager(channel.guild)
        if (position < 1) return RichResponse("Invalid position", RichResponse.Type.USER_ERROR, false)

        val queue = musicManager.scheduler.queue.toList()
        if (position > 1 && (position - 2) >= queue.size) {
            return RichResponse("Position $position doesn't exist in queue", RichResponse.Type.USER_ERROR, false)
        }

        val trackToSkip = if (position == 1) {
            musicManager.player.playingTrack
        } else {
            queue[position - 2]
        }

        if (trackToSkip == null) {
            return RichResponse("Nothing to skip", RichResponse.Type.USER_ERROR, false)
        }

        if (position == 1) {
            musicManager.scheduler.nextTrack()
        } else {
            musicManager.scheduler.skipTrack(position - 1)
        }

        return RichResponse(
            title = if (position == 1) "Skipping current track" else "Removing track from queue",
            text = "[${trackToSkip.info.title}](${trackToSkip.info.uri})",
            thumbnail = if (trackToSkip.info.uri.contains("youtube.com")) {
                "https://img.youtube.com/vi/${
                    net.andrecarbajal.naviMusic.util.URLUtils.getURLParam(
                        trackToSkip.info.uri, "v"
                    ).orElse("")
                }/maxresdefault.jpg"
            } else null
        )
    }

    fun clear(guild: Guild): RichResponse {
        val musicManager = getGuildMusicManager(guild)
        musicManager.scheduler.clear()
        return RichResponse(
            title = "Queue cleaned", text = "All songs have been removed and playback stopped."
        )
    }

    fun play(musicManager: GuildMusicManager, track: AudioTrack, member: Member?) {
        track.userData = member
        musicManager.scheduler.queue(track)
    }

    fun playPlaylist(musicManager: GuildMusicManager, playlist: AudioPlaylist, member: Member?) {
        playlist.tracks.forEach { track ->
            track.userData = member
            musicManager.scheduler.queue(track)
        }
    }

    fun connectToChannel(audioManager: AudioManager, member: Member?, isInitial: Boolean): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        if (audioManager.isConnected) {
            future.complete(true)
            return future
        }

        if (!isInitial) {
            future.complete(false)
            return future
        }

        val voiceChannel = member?.voiceState?.channel
        if (voiceChannel == null) {
            future.complete(false)
            return future
        }

        return try {
            log.info("Connecting to channel: {}", voiceChannel.name)
            audioManager.openAudioConnection(voiceChannel)

            Thread.ofVirtual().start {
                try {
                    var retries = 0
                    while (!audioManager.isConnected && retries < 100) {
                        Thread.sleep(100)
                        retries++
                    }

                    if (audioManager.isConnected) {
                        log.info("Connected successfully, waiting for DAVE session...")
                        Thread.sleep(2000)
                        future.complete(true)
                    } else {
                        log.warn("Connection timeout")
                        future.complete(false)
                    }
                } catch (e: Exception) {
                    log.error("Error waiting for connection", e)
                    future.complete(false)
                }
            }
            future
        } catch (ex: Exception) {
            log.error("Error joining voice channel", ex)
            future.complete(false)
            future
        }
    }

    fun nowPlaying(textChannel: TextChannel): RichResponse {
        val musicManager = getGuildMusicManager(textChannel.guild)
        val track =
            musicManager.player.playingTrack ?: return RichResponse("No track playing", RichResponse.Type.ERROR, false)

        val trackInfo = track.info
        val richResponse = RichResponse(
            title = "Now playing",
            text = "[${trackInfo.title.trim()}](${trackInfo.uri}) de `${trackInfo.author}`",
            fields = listOf(
                MessageEmbed.Field(
                    "Duration", net.andrecarbajal.naviMusic.dto.VideoInfo(trackInfo).durationToReadable(), true
                ), MessageEmbed.Field(
                    "Position", "${net.andrecarbajal.naviMusic.dto.VideoInfo.formatTime(track.position)} / ${
                        net.andrecarbajal.naviMusic.dto.VideoInfo.formatTime(track.duration)
                    }", true
                )
            )
        )

        if (trackInfo.uri.contains("youtube.com")) {
            net.andrecarbajal.naviMusic.util.URLUtils.getURLParam(trackInfo.uri, "v").ifPresent { s ->
                richResponse.thumbnail = "https://img.youtube.com/vi/$s/maxresdefault.jpg"
            }
        }

        val member = track.getUserData(Member::class.java)
        if (member != null) {
            richResponse.footer = RichResponse.Footer(
                text = "Requested by ${member.effectiveName}", imageUrl = member.effectiveAvatarUrl
            )
        }

        return richResponse
    }

    fun pause(textChannel: TextChannel): RichResponse {
        val musicManager = getGuildMusicManager(textChannel.guild)
        val player = musicManager.player
        if (player.isPaused) return RichResponse("Already paused", RichResponse.Type.USER_ERROR, false)

        player.isPaused = true
        return RichResponse(
            title = "Playback Paused", text = "The current track has been paused. Use `/resume` to continue."
        )
    }

    fun resume(textChannel: TextChannel): RichResponse {
        val musicManager = getGuildMusicManager(textChannel.guild)
        val player = musicManager.player
        if (!player.isPaused) return RichResponse("Already playing", RichResponse.Type.USER_ERROR, false)

        player.isPaused = false
        return RichResponse(
            title = "Playback Resumed", text = "Resuming playback of the current track."
        )
    }
}
