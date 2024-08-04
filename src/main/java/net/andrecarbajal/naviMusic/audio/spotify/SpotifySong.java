package net.andrecarbajal.naviMusic.audio.spotify;

import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public class SpotifySong {

    private String title;
    private ArtistSimplified[] artists;

    public SpotifySong() {
        this.title = "";
        this.artists = new ArtistSimplified[]{};
    }

    public SpotifySong(String title, ArtistSimplified[] artists){
        this.title = title;
        this.artists = artists;
    }

    public String getTitle(){ return this.title; }
    public ArtistSimplified[] getArtist() { return this.artists; }

    public void setTitle(String title) { this.title = title; }
    public void setArtist(ArtistSimplified[] artists) { this.artists = artists; }

    public String toString() {
        StringBuilder artists = new StringBuilder();
        for (ArtistSimplified artist : this.artists)
            artists.append(String.format("%s ", artist.getName()));

        return String.format("%s %s lyrics", this.title, artists);
    }
}
