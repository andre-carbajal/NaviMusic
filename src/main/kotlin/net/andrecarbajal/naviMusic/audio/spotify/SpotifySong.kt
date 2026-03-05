package net.andrecarbajal.naviMusic.audio.spotify

import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified

data class SpotifySong(val title: String, val artists: Array<ArtistSimplified>) {

    fun getArtistsString(): String {
        return artists.joinToString(" - ") { it.name }
    }

    override fun toString(): String {
        val artistsNames = artists.joinToString(" ") { it.name }
        return "$title $artistsNames"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpotifySong

        if (title != other.title) return false
        if (!artists.contentEquals(other.artists)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artists.contentHashCode()
        return result
    }
}
