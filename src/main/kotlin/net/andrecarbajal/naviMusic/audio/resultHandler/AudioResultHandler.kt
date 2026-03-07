package net.andrecarbajal.naviMusic.audio.resultHandler

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.lavalink.youtube.YoutubeAudioSourceManager
import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.dto.VideoInfo
import net.andrecarbajal.naviMusic.dto.response.Response
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.andrecarbajal.naviMusic.util.URLUtils
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

class AudioResultHandler(
    private val musicService: MusicService,
    private val guild: Guild,
    private val member: Member,
    private val event: SlashCommandInteractionEvent? = null
) : AudioLoadResultHandler {

    private val log = LoggerFactory.getLogger(AudioResultHandler::class.java)
    var response: Response? = null
        private set

    override fun trackLoaded(track: AudioTrack) {
        val trackInfo = track.info
        val size = musicService.getGuildMusicManager(guild).scheduler.getQueueSize() + 1

        val richResponse = RichResponse(
            title = "Song added to queue",
            text = "[${trackInfo.title.trim()}](${trackInfo.uri}) de `${trackInfo.author}`",
            fields = listOf(
                MessageEmbed.Field("Duration", VideoInfo(trackInfo).durationToReadable(), true),
                MessageEmbed.Field("In queue", if (size == 1) "1 song" else "$size songs", true)
            ),
            footer = RichResponse.Footer(
                text = "Added by ${member.effectiveName}", imageUrl = member.effectiveAvatarUrl
            )
        )

        if (track.sourceManager is YoutubeAudioSourceManager) {
            URLUtils.getURLParam(trackInfo.uri, "v").ifPresent { s ->
                richResponse.thumbnail = "https://img.youtube.com/vi/$s/maxresdefault.jpg"
            }
        }

        response = richResponse
        event?.let { richResponse.editReply(it) }
        musicService.play(musicService.getGuildMusicManager(guild), track, member)
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        val firstTrack = playlist.selectedTrack ?: playlist.tracks.firstOrNull() ?: return

        if (playlist.isSearchResult) {
            trackLoaded(firstTrack)
            return
        }

        val playlistSize = playlist.tracks.size
        val size = musicService.getGuildMusicManager(guild).scheduler.getQueueSize() + playlistSize

        val richResponse = RichResponse(
            title = "Playlist added to queue", text = playlist.name, fields = listOf(
                MessageEmbed.Field("Songs added", playlistSize.toString(), true),
                MessageEmbed.Field("In queue", if (size == 1) "1 song" else "$size songs", true)
            ), footer = RichResponse.Footer(
                text = "Added by ${member.effectiveName}", imageUrl = member.effectiveAvatarUrl
            )
        )

        if (firstTrack.sourceManager is YoutubeAudioSourceManager) {
            URLUtils.getURLParam(firstTrack.info.uri, "v").ifPresent { s ->
                richResponse.thumbnail = "https://img.youtube.com/vi/$s/maxresdefault.jpg"
            }
        }

        response = richResponse
        event?.let { richResponse.editReply(it) }
        musicService.playPlaylist(musicService.getGuildMusicManager(guild), playlist, member)
    }

    override fun noMatches() {
        val richResponse = RichResponse(
            type = Response.Type.USER_ERROR, text = "Nothing found"
        )
        response = richResponse
        event?.let { richResponse.editReply(it) }
    }

    override fun loadFailed(exception: FriendlyException) {
        log.error("Error loading track", exception)
        val richResponse = RichResponse(
            type = Response.Type.ERROR, text = "Internal error"
        )
        response = richResponse
        event?.let { richResponse.editReply(it) }
    }
}
