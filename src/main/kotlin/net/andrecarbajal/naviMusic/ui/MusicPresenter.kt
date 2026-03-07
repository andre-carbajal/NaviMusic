package net.andrecarbajal.naviMusic.ui

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyPlaylist
import net.andrecarbajal.naviMusic.dto.VideoInfo
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.andrecarbajal.naviMusic.util.URLUtils
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import org.springframework.stereotype.Component

@Component
class MusicPresenter {

    fun formatNowPlaying(track: AudioTrack): RichResponse {
        val trackInfo = track.info
        val richResponse = RichResponse(
            title = "Now playing",
            text = "[${trackInfo.title.trim()}](${trackInfo.uri}) by `${trackInfo.author}`",
            fields = listOf(
                MessageEmbed.Field("Duration", VideoInfo(trackInfo).durationToReadable(), true),
                MessageEmbed.Field(
                    "Position",
                    "${VideoInfo.formatTime(track.position)} / ${VideoInfo.formatTime(track.duration)}",
                    true
                )
            )
        )

        if (trackInfo.uri.contains("youtube.com")) {
            URLUtils.getURLParam(trackInfo.uri, "v").ifPresent { s ->
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

    fun formatSpotifyResponse(
        member: Member?,
        playlist: SpotifyPlaylist,
        type: String,
        url: String,
        currentQueueSize: Int
    ): RichResponse {
        val playlistSize = playlist.songs.size
        val totalQueueSize = currentQueueSize + playlistSize

        return RichResponse(
            title = "Adding Spotify $type to queue",
            text = "[${playlist.title}]($url)",
            fields = listOf(
                MessageEmbed.Field("Songs", "$playlistSize songs", true),
                MessageEmbed.Field("In queue", if (totalQueueSize == 1) "1 song" else "$totalQueueSize songs", true)
            ),
            footer = member?.let {
                RichResponse.Footer(text = "Added by ${it.effectiveName}", imageUrl = it.effectiveAvatarUrl)
            }
        )
    }

    fun formatSimpleResponse(
        title: String,
        text: String,
        type: RichResponse.Type = RichResponse.Type.OK
    ): RichResponse {
        return RichResponse(title = title, text = text, type = type)
    }
}
