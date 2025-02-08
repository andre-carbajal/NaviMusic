package net.andrecarbajal.naviMusic.audio.spotify;

import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public record SpotifySong(String title, ArtistSimplified[] artists) {

    public String getArtists() {
        StringBuilder artists = new StringBuilder();
        for (int i = 0; i < this.artists.length; i++) {
            artists.append(this.artists[i].getName());
            if (i < this.artists.length - 1) {
                artists.append(" - ");
            }
        }
        return artists.toString();
    }

    public String toString() {
        StringBuilder artists = new StringBuilder();
        for (ArtistSimplified artist : this.artists)
            artists.append(String.format("%s ", artist.getName()));

        return String.format("%s %s", this.title, artists);
    }
}
