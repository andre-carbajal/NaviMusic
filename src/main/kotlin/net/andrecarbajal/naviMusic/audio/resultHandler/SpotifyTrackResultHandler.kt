package net.andrecarbajal.naviMusic.audio.resultHandler

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.audio.spotify.SpotifySong
import net.andrecarbajal.naviMusic.dto.VideoInfo
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

class SpotifyTrackResultHandler(
    private val musicService: MusicService,
    private val guild: Guild,
    private val member: Member?,
    private val song: SpotifySong,
    private val event: SlashCommandInteractionEvent? = null
) : AudioLoadResultHandler {

    private val log = LoggerFactory.getLogger(SpotifyTrackResultHandler::class.java)
    var response: RichResponse? = null
        private set

    override fun trackLoaded(track: AudioTrack) {
        val richResponse = RichResponse(title = "Not Spotify URL")
        response = richResponse
        event?.let { richResponse.editReply(it) }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        if (playlist.isSearchResult) {
            val firstTrack = playlist.tracks.firstOrNull() ?: return
            val size = musicService.getGuildMusicManager(guild).scheduler.getQueueSize() + 1

            val richResponse = RichResponse(
                title = "Adding Spotify song to queue",
                text = "${song.title} by `${song.getArtistsString()}`",
                fields = listOf(
                    MessageEmbed.Field("Duration", VideoInfo(firstTrack.info).durationToReadable(), true),
                    MessageEmbed.Field("In queue", if (size == 1) "1 song" else "$size songs", true)
                ),
                footer = RichResponse.Footer(
                    text = "Added by ${member?.effectiveName ?: "unknown"}", imageUrl = member?.effectiveAvatarUrl
                )
            )
            response = richResponse
            event?.let { richResponse.editReply(it) }
            musicService.play(musicService.getGuildMusicManager(guild), firstTrack, member)
        }
    }

    override fun noMatches() {
        val richResponse = RichResponse(
            type = RichResponse.Type.USER_ERROR, text = "Nothing found"
        )
        response = richResponse
        event?.let { richResponse.editReply(it) }
    }

    override fun loadFailed(exception: FriendlyException) {
        log.error("Error loading spotify track", exception)
        val richResponse = RichResponse(
            type = RichResponse.Type.ERROR, text = "Internal error"
        )
        response = richResponse
        event?.let { richResponse.editReply(it) }
    }
}
