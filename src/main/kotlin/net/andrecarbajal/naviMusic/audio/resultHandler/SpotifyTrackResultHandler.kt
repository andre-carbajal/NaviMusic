package net.andrecarbajal.naviMusic.audio.resultHandler

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.audio.spotify.SpotifySong
import net.andrecarbajal.naviMusic.dto.VideoInfo
import net.andrecarbajal.naviMusic.dto.response.Response
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory

class SpotifyTrackResultHandler(
    private val musicService: MusicService,
    private val guild: Guild,
    private val member: Member?,
    private val song: SpotifySong
) : AudioLoadResultHandler {

    private val log = LoggerFactory.getLogger(SpotifyTrackResultHandler::class.java)
    var response: Response? = null
        private set

    override fun trackLoaded(track: AudioTrack) {
        response = RichResponse(title = "Not Spotify URL")
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        if (playlist.isSearchResult) {
            val firstTrack = playlist.tracks.firstOrNull() ?: return
            val size = musicService.getGuildMusicManager(guild).scheduler.getQueueSize() + 1

            response = RichResponse(
                title = "Adding Spotify song to queue",
                text = "${song.title} by `${song.getArtistsString()}`",
                fields = listOf(
                    MessageEmbed.Field("Duration", VideoInfo(firstTrack.info).durationToReadable(), true),
                    MessageEmbed.Field("In queue", if (size == 1) "1 song" else "$size songs", true)
                ),
                footer = RichResponse.Footer(
                    text = "Added by ${member?.effectiveName ?: "unknown"}",
                    imageUrl = member?.effectiveAvatarUrl
                )
            )
            musicService.play(guild, musicService.getGuildMusicManager(guild), firstTrack, member)
        }
    }

    override fun noMatches() {
        response = RichResponse(
            type = Response.Type.USER_ERROR,
            text = "Nothing found"
        )
    }

    override fun loadFailed(exception: FriendlyException) {
        log.error("Error loading spotify track", exception)
        response = RichResponse(
            type = Response.Type.ERROR,
            text = "Internal error"
        )
    }
}
