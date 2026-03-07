package net.andrecarbajal.naviMusic.audio.spotify

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import se.michaelthelin.spotify.model_objects.specification.Track
import kotlin.math.ceil

@Component
class SpotifyFetch(private val tokenProvider: SpotifyTokenProvider) {

    private val log = LoggerFactory.getLogger(SpotifyFetch::class.java)

    fun fetchSong(id: String): SpotifySong? {
        return try {
            val spotify = tokenProvider.getSpotifyApi()
            val trackFuture = spotify.getTrack(id).build().executeAsync()
            val track = trackFuture.join()
            SpotifySong(track.name, track.artists)
        } catch (e: Exception) {
            log.error("Error when fetching spotify song `{}`: {}", id, e.message)
            null
        }
    }

    fun fetchPlaylist(id: String): SpotifyPlaylist? {
        return try {
            val spotify = tokenProvider.getSpotifyApi()
            val playlistFuture = spotify.getPlaylist(id).build().executeAsync()
            val playlist = playlistFuture.join()

            val songIDs = playlist.tracks.items.map { it.track.id }
            val tracks = mutableListOf<Track>()

            if (songIDs.size > 50) {
                val iterations = ceil(songIDs.size.toDouble() / 50).toInt()
                for (i in 1..iterations) {
                    val batch = songIDs.subList(50 * (i - 1), (50 * i).coerceAtMost(songIDs.size)).joinToString(",")
                    val trackFuture = spotify.getSeveralTracks(batch).build().executeAsync()
                    tracks.addAll(trackFuture.join())
                }
            } else if (songIDs.isNotEmpty()) {
                val trackFuture = spotify.getSeveralTracks(songIDs.joinToString(",")).build().executeAsync()
                tracks.addAll(trackFuture.join())
            }

            val songs = tracks.map { SpotifySong(it.name, it.artists) }
            SpotifyPlaylist(playlist.name, songs.toTypedArray())
        } catch (e: Exception) {
            log.error("Error when fetching spotify playlist `{}`: {}", id, e.message)
            null
        }
    }

    fun fetchAlbum(id: String): SpotifyPlaylist? {
        return try {
            val spotify = tokenProvider.getSpotifyApi()
            val albumFuture = spotify.getAlbum(id).build().executeAsync()
            val album = albumFuture.get()

            val songs = album.tracks.items.map { SpotifySong(it.name, it.artists) }
            SpotifyPlaylist(album.name, songs.toTypedArray())
        } catch (e: Exception) {
            log.error("Error when fetching spotify album `{}`: {}", id, e.message)
            null
        }
    }
}
