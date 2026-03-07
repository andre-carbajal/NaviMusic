package net.andrecarbajal.naviMusic.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.andrecarbajal.naviMusic.audio.resultHandler.AudioResultHandler
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyPlaylistResultHandler
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyTrackResultHandler
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyFetch
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyPlaylist
import net.andrecarbajal.naviMusic.audio.spotify.SpotifySong
import net.andrecarbajal.naviMusic.dto.response.Response
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
    ): Response {
        val musicManager = getGuildMusicManager(channel.guild)
        val audioResult = AudioResultHandler(this, channel.guild, member!!, event)

        connectToChannel(channel.guild.audioManager, member, true).thenAccept { connected ->
            if (connected) {
                audioPlayerManager.loadItemOrdered(musicManager, "$provider: $track", audioResult)
            }
        }

        return Response("Loading track...", Response.Type.OK, false)
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadAndPlayUrl(
        channel: TextChannel, trackUrl: String, member: Member?, event: SlashCommandInteractionEvent? = null
    ): Response {
        val musicManager = getGuildMusicManager(channel.guild)
        val audioResult = AudioResultHandler(this, channel.guild, member!!, event)

        connectToChannel(channel.guild.audioManager, member, true).thenAccept { connected ->
            if (connected) {
                audioPlayerManager.loadItemOrdered(musicManager, trackUrl, audioResult)
            }
        }

        return Response("Loading track from URL...", Response.Type.OK, false)
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadAndPlaySpotifyUrl(
        channel: TextChannel,
        provider: String,
        trackUrl: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): Response {
        val musicManager = getGuildMusicManager(channel.guild)

        connectToChannel(channel.guild.audioManager, member, true).thenAccept { connected ->
            if (connected) {
                if (trackUrl.contains("track", ignoreCase = true)) {
                    val song = spotifyFetch.fetchSong(trackUrl) ?: return@thenAccept
                    val spotifyTrackResultHandler = SpotifyTrackResultHandler(this, channel.guild, member, song, event)
                    audioPlayerManager.loadItemOrdered(musicManager, "$provider: $song", spotifyTrackResultHandler)
                }

                if (trackUrl.contains("playlist", ignoreCase = true)) {
                    val playlist = spotifyFetch.fetchPlaylist(trackUrl) ?: return@thenAccept
                    loadSpotifySongs(playlist, channel, member, provider)
                    event?.let { spotifyResponse(channel.guild, member, playlist, "playlist").editReply(it) }
                }

                if (trackUrl.contains("album", ignoreCase = true)) {
                    val playlist = spotifyFetch.fetchAlbum(trackUrl) ?: return@thenAccept
                    loadSpotifySongs(playlist, channel, member, provider)
                    event?.let { spotifyResponse(channel.guild, member, playlist, "album").editReply(it) }
                }
            }
        }

        return Response("Loading Spotify content...", Response.Type.OK, false)
    }

    private fun spotifyResponse(guild: Guild, member: Member?, playlist: SpotifyPlaylist, type: String): Response {
        val playlistSize = playlist.songs.size
        val sizeInQueue = getGuildMusicManager(guild).scheduler.getQueueSize() + playlistSize

        return RichResponse(
            title = "Adding Spotify $type to queue", text = playlist.title, fields = listOf(
                MessageEmbed.Field("Songs", "$playlistSize songs", true),
                MessageEmbed.Field("In queue", if (sizeInQueue == 1) "1 song" else "$sizeInQueue songs", true)
            ), footer = member?.let {
                RichResponse.Footer(text = "Added by ${it.effectiveName}", imageUrl = it.effectiveAvatarUrl)
            })
    }

    private fun loadSpotifySongs(playlist: SpotifyPlaylist, channel: TextChannel, member: Member?, provider: String) {
        playlist.songs.forEach { song: SpotifySong ->
            log.warn("Loading song: {}", song.toString())
            val musicManager = getGuildMusicManager(channel.guild)
            val spotifyPlaylistResultHandler = SpotifyPlaylistResultHandler(this, channel.guild, member)
            audioPlayerManager.loadItemOrdered(musicManager, "$provider: $song", spotifyPlaylistResultHandler)
        }
    }

    fun skipTrack(channel: TextChannel, position: Int): Response {
        val musicManager = getGuildMusicManager(channel.guild)
        if (position < 1) return Response("Invalid position", Response.Type.ERROR, false)

        if (position == 1) {
            musicManager.scheduler.nextTrack()
            return Response("Skipped", Response.Type.OK, false)
        }

        musicManager.scheduler.skipTrack(position - 1)
        return Response("Skipping song $position", Response.Type.OK, false)
    }

    fun clear(guild: Guild): Response {
        val musicManager = getGuildMusicManager(guild)
        musicManager.scheduler.clear()
        return Response("Cleaning...", Response.Type.OK, false)
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

    fun nowPlaying(textChannel: TextChannel): Response {
        val musicManager = getGuildMusicManager(textChannel.guild)
        val track = musicManager.player.playingTrack ?: return Response("No track playing", Response.Type.ERROR, false)
        return Response("Now playing: ${track.info.title}", Response.Type.OK, false)
    }

    fun pause(textChannel: TextChannel): Response {
        val musicManager = getGuildMusicManager(textChannel.guild)
        val player = musicManager.player
        if (player.isPaused) return Response("Already paused", Response.Type.ERROR, false)

        player.isPaused = true
        return Response("Paused", Response.Type.OK, false)
    }

    fun resume(textChannel: TextChannel): Response {
        val musicManager = getGuildMusicManager(textChannel.guild)
        val player = musicManager.player
        if (!player.isPaused) return Response("Already playing", Response.Type.ERROR, false)

        player.isPaused = false
        return Response("Resumed ", Response.Type.OK, false)
    }
}
