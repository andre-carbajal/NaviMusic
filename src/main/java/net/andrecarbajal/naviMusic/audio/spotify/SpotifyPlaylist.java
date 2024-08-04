package net.andrecarbajal.naviMusic.audio.spotify;

import java.util.ArrayList;
import java.util.List;

public class SpotifyPlaylist {
    private String title;
    private SpotifySong[] songs;

    public SpotifyPlaylist(String title, SpotifySong[] songs) {
        this.title = title;
        this.songs = songs;
    }

    public String getTitle() { return this.title; }
    public SpotifySong[] getSongs() { return this.songs; }

    public void setTitle(String title){ this.title = title; }
    public void setSongs(SpotifySong[] songs) { this.songs = songs; }

    public String[] toStringSongs() {
        List<String> result = new ArrayList<>();
        for (SpotifySong song : songs){
            result.add(song.toString());
        }
        return result.toArray(String[]::new);
    }
}
