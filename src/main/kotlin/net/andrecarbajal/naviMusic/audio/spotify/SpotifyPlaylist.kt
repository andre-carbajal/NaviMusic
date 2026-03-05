package net.andrecarbajal.naviMusic.audio.spotify

data class SpotifyPlaylist(val title: String, val songs: Array<SpotifySong>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpotifyPlaylist

        if (title != other.title) return false
        if (!songs.contentEquals(other.songs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + songs.contentHashCode()
        return result
    }
}
