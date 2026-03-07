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
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class MusicService(
    @Lazy private val audioPlayerManager: AudioPlayerManager,
    private val spotifyFetch: SpotifyFetch,
    private val presenter: MusicPresenter
) {

    private val log = LoggerFactory.getLogger(MusicService::class.java)
    private val managers = mutableMapOf<Long, GuildMusicManager>()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    fun getGuildMusicManager(guild: Guild): GuildMusicManager {
        val musicManager = managers.getOrPut(guild.idLong) {
            GuildMusicManager(audioPlayerManager, guild)
        }
        guild.audioManager.sendingHandler = musicManager.getSendHandler()
        return musicManager
    }

    fun loadAndPlay(
        channel: TextChannel,
        provider: String,
        track: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        val query = "$provider: $track"
        loadItem(channel.guild, member, query, AudioResultHandler(this, channel.guild, member!!, event, null))
        return presenter.formatSimpleResponse("Loading...", "Adding track to queue")
    }

    fun loadAndPlayUrl(
        channel: TextChannel, trackUrl: String, member: Member?, event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        loadItem(channel.guild, member, trackUrl, AudioResultHandler(this, channel.guild, member!!, event, trackUrl))
        return presenter.formatSimpleResponse("Loading URL...", "Fetching data from provided link")
    }

    fun loadAndPlaySpotifyUrl(
        channel: TextChannel,
        provider: String,
        trackUrl: String,
        member: Member?,
        event: SlashCommandInteractionEvent? = null
    ): RichResponse {
        val connectionFuture = connectToChannel(channel.guild.audioManager, member)

        serviceScope.launch {
            try {
                when (val resource = SpotifyResource.fromUrl(trackUrl)) {
                    is SpotifyResource.Track -> handleSpotifyTrack(
                        resource,
                        provider,
                        channel,
                        member,
                        event,
                        connectionFuture
                    )

                    is SpotifyResource.Playlist, is SpotifyResource.Album -> handleSpotifyCollection(
                        resource,
                        provider,
                        channel,
                        member,
                        event,
                        connectionFuture,
                        trackUrl
                    )

                    is SpotifyResource.Invalid -> log.warn("Invalid Spotify URL: {}", trackUrl)
                }
            } catch (e: Exception) {
                log.error("Error processing Spotify URL", e)
            }
        }

        return presenter.formatSimpleResponse("Spotify Processing", "Analyzing Spotify link...")
    }

    private fun handleSpotifyTrack(
        resource: SpotifyResource.Track, provider: String, channel: TextChannel, member: Member?,
        event: SlashCommandInteractionEvent?, connectionFuture: CompletableFuture<Boolean>
    ) {
        val song = spotifyFetch.fetchSong(resource.id) ?: return
        connectionFuture.thenAccept { connected ->
            if (connected) {
                val handler = SpotifyTrackResultHandler(this, channel.guild, member, song, event)
                audioPlayerManager.loadItemOrdered(getGuildMusicManager(channel.guild), "$provider: $song", handler)
            }
        }
    }

    private fun handleSpotifyCollection(
        resource: SpotifyResource, provider: String, channel: TextChannel, member: Member?,
        event: SlashCommandInteractionEvent?, connectionFuture: CompletableFuture<Boolean>, trackUrl: String
    ) {
        val playlist =
            if (resource is SpotifyResource.Playlist) spotifyFetch.fetchPlaylist(resource.id) else spotifyFetch.fetchAlbum(
                (resource as SpotifyResource.Album).id
            )

        if (playlist != null) {
            connectionFuture.thenAccept { connected ->
                if (connected) {
                    loadSpotifySongs(playlist, channel, member, provider)
                    event?.let {
                        val type = if (resource is SpotifyResource.Playlist) "playlist" else "album"
                        val queueSize = getGuildMusicManager(channel.guild).scheduler.getQueueSize()
                        presenter.formatSpotifyResponse(member, playlist, type, trackUrl, queueSize).editReply(it)
                    }
                }
            }
        }
    }

    private fun loadItem(guild: Guild, member: Member?, identifier: String, handler: AudioLoadResultHandler) {
        val musicManager = getGuildMusicManager(guild)
        connectToChannel(guild.audioManager, member).thenAccept { connected ->
            if (connected) {
                audioPlayerManager.loadItemOrdered(musicManager, identifier, handler)
            }
        }
    }

    private fun loadSpotifySongs(playlist: SpotifyPlaylist, channel: TextChannel, member: Member?, provider: String) {
        playlist.songs.forEach { song: SpotifySong ->
            val musicManager = getGuildMusicManager(channel.guild)
            val handler = SpotifyPlaylistResultHandler(this, channel.guild, member)
            audioPlayerManager.loadItemOrdered(musicManager, "$provider: $song", handler)
        }
    }

    fun skipTrack(channel: TextChannel, position: Int): RichResponse {
        val musicManager = getGuildMusicManager(channel.guild)
        if (position < 1) return presenter.formatSimpleResponse(
            "Error",
            "Invalid position",
            RichResponse.Type.USER_ERROR
        )

        val queue = musicManager.scheduler.queue.toList()
        if (position > 1 && (position - 2) >= queue.size) {
            return presenter.formatSimpleResponse(
                "Error",
                "Position $position doesn't exist",
                RichResponse.Type.USER_ERROR
            )
        }

        val trackToSkip = if (position == 1) musicManager.player.playingTrack else queue[position - 2]
        if (trackToSkip == null) return presenter.formatSimpleResponse(
            "Error",
            "Nothing to skip",
            RichResponse.Type.USER_ERROR
        )

        if (position == 1) musicManager.scheduler.nextTrack() else musicManager.scheduler.skipTrack(position - 1)

        return presenter.formatSimpleResponse(
            "Skipping...",
            "Removed: [${trackToSkip.info.title}](${trackToSkip.info.uri})"
        )
    }

    fun clear(guild: Guild): RichResponse {
        getGuildMusicManager(guild).scheduler.clear()
        return presenter.formatSimpleResponse("Queue cleaned", "All songs removed.")
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

    fun connectToChannel(audioManager: AudioManager, member: Member?): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (audioManager.isConnected) {
            future.complete(true)
            return future
        }
        val voiceChannel = member?.voiceState?.channel ?: run {
            future.complete(false)
            return future
        }

        try {
            audioManager.openAudioConnection(voiceChannel)
            serviceScope.launch {
                var retries = 0
                while (!audioManager.isConnected && retries < 100) {
                    delay(100)
                    retries++
                }
                if (audioManager.isConnected) {
                    delay(2000)
                    future.complete(true)
                } else {
                    future.complete(false)
                }
            }
        } catch (ex: Exception) {
            log.error("Error joining voice channel", ex)
            future.complete(false)
        }
        return future
    }

    fun nowPlaying(textChannel: TextChannel): RichResponse {
        val musicManager = getGuildMusicManager(textChannel.guild)
        val track = musicManager.player.playingTrack ?: return presenter.formatSimpleResponse(
            "No music",
            "The queue is empty.",
            RichResponse.Type.ERROR
        )
        return presenter.formatNowPlaying(track)
    }

    fun pause(textChannel: TextChannel): RichResponse {
        val player = getGuildMusicManager(textChannel.guild).player
        if (player.isPaused) return presenter.formatSimpleResponse(
            "Error",
            "Already paused",
            RichResponse.Type.USER_ERROR
        )
        player.isPaused = true
        return presenter.formatSimpleResponse("Paused", "Use /resume to continue")
    }

    fun resume(textChannel: TextChannel): RichResponse {
        val player = getGuildMusicManager(textChannel.guild).player
        if (!player.isPaused) return presenter.formatSimpleResponse(
            "Error",
            "Already playing",
            RichResponse.Type.USER_ERROR
        )
        player.isPaused = false
        return presenter.formatSimpleResponse("Resumed", "Playback resumed")
    }
}
