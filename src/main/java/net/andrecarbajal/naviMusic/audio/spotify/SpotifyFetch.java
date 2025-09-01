package net.andrecarbajal.naviMusic.audio.spotify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SpotifyFetch {
    private final SpotifyTokenProvider tokenProvider;

    public SpotifyFetch(SpotifyTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public SpotifySong fetchSong(String url) {
        try {
            String id = getID(url);
            SpotifyApi spotify = tokenProvider.getSpotifyApi();
            CompletableFuture<Track> trackFuture = spotify.getTrack(id).build().executeAsync();
            Track track = trackFuture.join();
            return new SpotifySong(track.getName(), track.getArtists());
        } catch (Exception e) {
            log.error("Error when fetching spotify song `{}`: {}", getID(url), e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylist fetchPlaylist(String url) {
        try {
            List<String> songIDs = new ArrayList<>();
            SpotifyApi spotify = tokenProvider.getSpotifyApi();
            CompletableFuture<Playlist> playlistFuture = spotify.getPlaylist(getID(url)).build().executeAsync();
            Playlist playlist = playlistFuture.join();
            for (PlaylistTrack track : playlist.getTracks().getItems())
                songIDs.add(track.getTrack().getId());

            List<Track> tracks = new ArrayList<>();
            if (songIDs.size() > 50) {
                for (int i = 1; i <= Math.ceil((double) songIDs.size() / 50); i++) {
                    String test = String.join(",", songIDs.subList(50 * (i - 1), Math.min(50 * i, songIDs.size())));
                    CompletableFuture<Track[]> trackFuture = spotify.getSeveralTracks(test).build().executeAsync();
                    Track[] temp = trackFuture.join();
                    Collections.addAll(tracks, temp);
                }
            } else if (!songIDs.isEmpty()) {
                String test = String.join(",", songIDs);
                CompletableFuture<Track[]> trackFuture = spotify.getSeveralTracks(test).build().executeAsync();
                Track[] temp = trackFuture.join();
                Collections.addAll(tracks, temp);
            }

            List<SpotifySong> songs = new ArrayList<>();
            for (Track track : tracks)
                songs.add(new SpotifySong(track.getName(), track.getArtists()));

            return new SpotifyPlaylist(playlist.getName(), songs.toArray(SpotifySong[]::new));
        } catch (Exception e) {
            log.error("Error when fetching spotify playlist `{}`: {}", getID(url), e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylist fetchAlbum(String url) {
        try {
            List<SpotifySong> songs = new ArrayList<>();
            SpotifyApi spotify = tokenProvider.getSpotifyApi();
            CompletableFuture<Album> albumFuture = spotify.getAlbum(getID(url)).build().executeAsync();
            Album album = albumFuture.get();
            for (TrackSimplified track : album.getTracks().getItems())
                songs.add(new SpotifySong(track.getName(), track.getArtists()));

            return new SpotifyPlaylist(album.getName(), songs.toArray(SpotifySong[]::new));
        } catch (Exception e) {
            log.error("Error when fetching spotify album `{}`: {}", getID(url), e.getMessage());
            return null;
        }
    }

    private String getID(String link) {
        if (!ifValidSpotifyLink(link)) return "";

        if (link.contains("?")) return link.substring(link.lastIndexOf("/") + 1, link.indexOf("?"));

        return link.substring(link.lastIndexOf("/"));
    }

    private boolean ifValidSpotifyLink(String link) {
        Pattern spotifyPattern = Pattern.compile("^(spotify:|https://[a-z]+\\.spotify\\.com/)");
        return spotifyPattern.matcher(link).find();
    }
}