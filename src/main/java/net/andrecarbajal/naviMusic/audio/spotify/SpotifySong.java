package net.andrecarbajal.naviMusic.audio.spotify;

import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public record SpotifySong(String title, ArtistSimplified[] artists) {
    public String toString() {
        StringBuilder artists = new StringBuilder();
        for (ArtistSimplified artist : this.artists)
            artists.append(String.format("%s ", artist.getName()));

        return String.format("%s %s lyrics", this.title, artists);
    }
}
