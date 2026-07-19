package net.andrecarbajal.naviMusic.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.andrecarbajal.naviMusic.audio.resultHandler.AudioResultHandler
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyPlaylistResultHandler
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyTrackResultHandler
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyFetch
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyPlaylist
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyResource
import net.andrecarbajal.naviMusic.audio.spotify.SpotifySong
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.andrecarbajal.naviMusic.ui.MusicPresenter
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

@Service
class MusicService(
    @Lazy private val audioPlayerManager: AudioPlayerManager,
    private val spotifyFetch: SpotifyFetch,
    private val presenter: MusicPresenter,
    @Value($$"${app.music.max-pending-loads:20}") private val maxPendingLoads: Int,
    @Value($$"${app.music.max-queue-size:500}") private val maxQueueSize: Int,
    @Value($$"${app.music.max-playlist-tracks:100}") private val maxPlaylistTracks: Int
) {
    private val log = LoggerFactory.getLogger(MusicService::class.java)
    private val managers = ConcurrentHashMap<Long, GuildMusicManager>()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    fun getGuildMusicManager(guild: Guild): GuildMusicManager = managers.computeIfAbsent(guild.idLong) {
        GuildMusicManager(audioPlayerManager, guild, maxPendingLoads) { releaseGuild(guild) }
    }.also { guild.audioManager.sendingHandler = it.getSendHandler() }

    fun releaseGuild(guild: Guild) {
        managers.remove(guild.idLong)?.shutdown()
    }

    fun loadAndPlay(
        channel: TextChannel,
        provider: String,
        track: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        val accepted = loadItem(channel.guild, member, "$provider: $track") { done ->
            AudioResultHandler(this, channel.guild, member!!, event, null, done)
        }
        return loadingResponse(accepted, "Loading...", "Adding track to queue")
    }

    fun loadAndPlayUrl(
        channel: TextChannel,
        trackUrl: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        val accepted = loadItem(channel.guild, member, trackUrl) { done ->
            AudioResultHandler(this, channel.guild, member!!, event, trackUrl, done)
        }
        return loadingResponse(accepted, "Loading URL...", "Fetching data from provided link")
    }

    fun loadAndPlaySpotifyUrl(
        channel: TextChannel,
        provider: String,
        trackUrl: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        if (!spotifyFetch.isConfigured()) return presenter.formatSimpleResponse(
            "Spotify No Configurado",
            "El bot no tiene configurada la API de Spotify.",
            RichResponse.Type.USER_ERROR
        )
        val manager = getGuildMusicManager(channel.guild)
        val accepted = manager.enqueueLoad { done ->
            serviceScope.launch {
                try {
                    when (val resource = SpotifyResource.fromUrl(trackUrl)) {
                        is SpotifyResource.Track -> spotifyFetch.fetchSong(resource.id)?.let { song ->
                            loadSpotifySong(channel.guild, member, provider, song, event, done)
                        } ?: done()

                        is SpotifyResource.Playlist, is SpotifyResource.Album -> {
                            val playlist =
                                if (resource is SpotifyResource.Playlist) spotifyFetch.fetchPlaylist(resource.id) else spotifyFetch.fetchAlbum(
                                    (resource as SpotifyResource.Album).id
                                )
                            if (playlist == null) done() else loadSpotifyPlaylist(
                                manager,
                                channel,
                                member,
                                provider,
                                playlist,
                                resource,
                                trackUrl,
                                event,
                                done
                            )
                        }

                        is SpotifyResource.Invalid -> done()
                    }
                } catch (exception: Exception) {
                    log.error("Error processing Spotify URL", exception)
                    done()
                }
            }
        }
        return loadingResponse(accepted, "Spotify Processing", "Analyzing Spotify link...")
    }

    private fun loadSpotifySong(
        guild: Guild,
        member: Member?,
        provider: String,
        song: SpotifySong,
        event: SlashCommandInteractionEvent?,
        done: () -> Unit
    ) {
        connectToChannel(guild.audioManager, member).thenAccept { connected ->
            if (!connected) done() else audioPlayerManager.loadItem(
                "$provider: $song",
                SpotifyTrackResultHandler(this, guild, member, song, event, done)
            )
        }
    }

    private fun loadSpotifyPlaylist(
        manager: GuildMusicManager,
        channel: TextChannel,
        member: Member?,
        provider: String,
        playlist: SpotifyPlaylist,
        resource: SpotifyResource,
        trackUrl: String,
        event: SlashCommandInteractionEvent?,
        done: () -> Unit
    ) {
        val songs = playlist.songs.take(maxPlaylistTracks)
        if (playlist.songs.size > maxPlaylistTracks) log.warn(
            "Spotify collection {} truncated to {} tracks",
            playlist.title,
            maxPlaylistTracks
        )
        connectToChannel(channel.guild.audioManager, member).thenAccept { connected ->
            if (!connected || songs.isEmpty()) {
                done(); return@thenAccept
            }
            event?.let {
                val type = if (resource is SpotifyResource.Playlist) "playlist" else "album"
                manager.submit { manager.scheduler.getQueueSize() }.thenAccept { size ->
                    presenter.formatSpotifyResponse(member, playlist, type, trackUrl, size).editReply(it)
                }
            }
            loadSpotifySongsSequentially(manager, channel.guild, member, provider, songs, 0, done)
        }
    }

    private fun loadSpotifySongsSequentially(
        manager: GuildMusicManager,
        guild: Guild,
        member: Member?,
        provider: String,
        songs: List<SpotifySong>,
        index: Int,
        done: () -> Unit
    ) {
        if (index >= songs.size) {
            done(); return
        }
        audioPlayerManager.loadItem("$provider: ${songs[index]}", SpotifyPlaylistResultHandler(this, guild, member) {
            loadSpotifySongsSequentially(manager, guild, member, provider, songs, index + 1, done)
        })
    }

    private fun loadItem(
        guild: Guild,
        member: Member?,
        identifier: String,
        handler: (() -> Unit) -> AudioLoadResultHandler
    ): Boolean {
        val manager = getGuildMusicManager(guild)
        return manager.enqueueLoad { done ->
            connectToChannel(guild.audioManager, member).thenAccept { connected ->
                if (connected) audioPlayerManager.loadItem(identifier, handler(done)) else done()
            }
        }
    }

    private fun loadingResponse(accepted: Boolean, title: String, text: String): RichResponse =
        if (accepted) presenter.formatSimpleResponse(title, text)
        else presenter.formatSimpleResponse(
            "Queue busy",
            "Too many pending requests in this server. Try again shortly.",
            RichResponse.Type.USER_ERROR
        )

    fun play(musicManager: GuildMusicManager, track: AudioTrack, member: Member?): CompletableFuture<Unit> =
        musicManager.submit {
            require(musicManager.scheduler.getQueueSize() < maxQueueSize) { "Queue limit reached" }
            track.userData = member
            musicManager.scheduler.queue(track)
        }

    fun playPlaylist(
        musicManager: GuildMusicManager,
        playlist: AudioPlaylist,
        member: Member?
    ): CompletableFuture<Unit> = musicManager.submit {
        val tracks = playlist.tracks.take(maxPlaylistTracks)
        require(musicManager.scheduler.getQueueSize() + tracks.size <= maxQueueSize) { "Queue limit reached" }
        tracks.forEach { it.userData = member; musicManager.scheduler.queue(it) }
    }

    fun skipTrack(channel: TextChannel, position: Int): RichResponse = getGuildMusicManager(channel.guild).submit {
        val scheduler = getGuildMusicManager(channel.guild).scheduler
        if (position < 1) return@submit presenter.formatSimpleResponse(
            "Error",
            "Invalid position",
            RichResponse.Type.USER_ERROR
        )
        val tracks = scheduler.queue.toList()
        val track =
            if (position == 1) getGuildMusicManager(channel.guild).player.playingTrack else tracks.getOrNull(position - 2)
        if (track == null) return@submit presenter.formatSimpleResponse(
            "Error",
            "Position $position doesn't exist",
            RichResponse.Type.USER_ERROR
        )
        if (position == 1) scheduler.nextTrack() else scheduler.skipTrack(position - 1)
        presenter.formatSimpleResponse("Skipping...", "Removed: [${track.info.title}](${track.info.uri})")
    }.join()

    fun clear(guild: Guild): RichResponse = getGuildMusicManager(guild).submit {
        getGuildMusicManager(guild).scheduler.clear()
        presenter.formatSimpleResponse("Queue cleaned", "All songs removed.")
    }.join()

    fun leave(guild: Guild) {
        val manager = managers[guild.idLong]
        manager?.submit { manager.scheduler.clear(); manager.player.stopTrack(); guild.audioManager.closeAudioConnection() }
            ?.join()
        releaseGuild(guild)
    }

    fun shuffle(guild: Guild) {
        getGuildMusicManager(guild).submit { getGuildMusicManager(guild).scheduler.shuffle() }
    }

    fun setRepeating(guild: Guild, repeating: Boolean): Boolean {
        val manager = getGuildMusicManager(guild)
        return manager.submit {
            val old = manager.scheduler.isRepeating; manager.scheduler.isRepeating = repeating; old
        }.join()
    }

    fun isRepeating(guild: Guild): Boolean {
        val manager = getGuildMusicManager(guild)
        return manager.submit { manager.scheduler.isRepeating }.join()
    }

    fun queueSnapshot(guild: Guild): Pair<AudioTrack?, List<AudioTrack>> {
        val manager = getGuildMusicManager(guild)
        return manager.submit { manager.player.playingTrack to manager.scheduler.queue.toList() }.join()
    }

    fun connectToChannel(audioManager: AudioManager, member: Member?): CompletableFuture<Boolean> {
        if (audioManager.isConnected) return CompletableFuture.completedFuture(true)
        val voiceChannel = member?.voiceState?.channel ?: return CompletableFuture.completedFuture(false)
        val future = CompletableFuture<Boolean>()
        try {
            audioManager.openAudioConnection(voiceChannel)
            serviceScope.launch {
                repeat(100) {
                    if (audioManager.isConnected) {
                        delay(2000.milliseconds); future.complete(true); return@launch
                    }; delay(100.milliseconds)
                }
                future.complete(false)
            }
        } catch (exception: Exception) {
            log.error("Error joining voice channel", exception); future.complete(false)
        }
        return future
    }

    fun nowPlaying(textChannel: TextChannel): RichResponse {
        val manager = getGuildMusicManager(textChannel.guild)
        return manager.submit {
            val track = manager.player.playingTrack ?: return@submit presenter.formatSimpleResponse(
                "No music",
                "The queue is empty.",
                RichResponse.Type.ERROR
            )
            presenter.formatNowPlaying(track)
        }.join()
    }

    fun pause(textChannel: TextChannel): RichResponse {
        val manager = getGuildMusicManager(textChannel.guild)
        return manager.submit {
            if (manager.player.isPaused) presenter.formatSimpleResponse(
                "Error",
                "Already paused",
                RichResponse.Type.USER_ERROR
            ) else {
                manager.player.isPaused = true; presenter.formatSimpleResponse("Paused", "Use /resume to continue")
            }
        }.join()
    }

    fun resume(textChannel: TextChannel): RichResponse {
        val manager = getGuildMusicManager(textChannel.guild)
        return manager.submit {
            if (!manager.player.isPaused) presenter.formatSimpleResponse(
                "Error",
                "Already playing",
                RichResponse.Type.USER_ERROR
            ) else {
                manager.player.isPaused = false; presenter.formatSimpleResponse("Resumed", "Playback resumed")
            }
        }.join()
    }
}
