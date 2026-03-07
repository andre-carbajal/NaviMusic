package net.andrecarbajal.naviMusic.audio.resultHandler

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import org.slf4j.LoggerFactory

class SpotifyPlaylistResultHandler(
    private val musicService: MusicService, private val guild: Guild, private val member: Member?
) : AudioLoadResultHandler {

    private val log = LoggerFactory.getLogger(SpotifyPlaylistResultHandler::class.java)
    var response: RichResponse? = null
        private set

    override fun trackLoaded(track: AudioTrack) {
        response = RichResponse(title = "Not Spotify URL")
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        if (playlist.isSearchResult) {
            val track = playlist.tracks.firstOrNull() ?: return
            musicService.play(musicService.getGuildMusicManager(guild), track, member)
        }
    }

    override fun noMatches() {
        response = RichResponse(
            type = RichResponse.Type.USER_ERROR, text = "Nothing found"
        )
    }

    override fun loadFailed(exception: FriendlyException) {
        log.error("Error loading spotify track", exception)
        response = RichResponse(
            type = RichResponse.Type.ERROR, text = "Internal error"
        )
    }
}
