package net.andrecarbajal.naviMusic.audio.spotify

sealed class SpotifyResource {
    data class Track(val id: String) : SpotifyResource()
    data class Playlist(val id: String) : SpotifyResource()
    data class Album(val id: String) : SpotifyResource()
    object Invalid : SpotifyResource()

    companion object {
        fun fromUrl(url: String): SpotifyResource {
            val cleanUrl = url.substringBefore("?")
            return when {
                cleanUrl.contains("/track/") -> Track(cleanUrl.substringAfter("/track/"))
                cleanUrl.contains("/playlist/") -> Playlist(cleanUrl.substringAfter("/playlist/"))
                cleanUrl.contains("/album/") -> Album(cleanUrl.substringAfter("/album/"))
                else -> Invalid
            }
        }
    }
}
