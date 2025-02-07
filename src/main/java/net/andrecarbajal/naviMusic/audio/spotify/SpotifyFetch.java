package net.andrecarbajal.naviMusic.audio.spotify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SpotifyFetch {
    private final SpotifyApi spotify;

    public SpotifyFetch(@Value("${app.spotify.clientId}") String clientId, @Value("${app.spotify.clientSecret}") String clientSecret) {
        this.spotify = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();

        try {
            ClientCredentialsRequest clientCredentialsRequest = spotify.clientCredentials().build();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotify.setAccessToken(clientCredentials.getAccessToken());
        } catch (Exception e) {
            log.error("Error init spotify api: {}", e.getMessage());
        }
    }


    public SpotifySong fetchSong(String url) {
        try {
            String id = getID(url);
            CompletableFuture<Track> trackFuture = spotify.getTrack(id).build().executeAsync();
            Track track = trackFuture.join();
            return new SpotifySong(track.getName(), track.getArtists(), uriToUrl(track.getUri()));
        } catch (Exception e) {
            log.error("Error when fetching spotify song `{}`: {}", getID(url), e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylist fetchPlaylist(String url) {
        try {
            List<String> songIDs = new ArrayList<>();
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
            }

            List<SpotifySong> songs = new ArrayList<>();
            for (Track track : tracks)
                songs.add(new SpotifySong(track.getName(), track.getArtists(), uriToUrl(track.getUri())));

            return new SpotifyPlaylist(playlist.getName(), songs.toArray(SpotifySong[]::new));
        } catch (Exception e) {
            log.error("Error when fetching spotify playlist `{}`: {}", getID(url), e.getMessage());
            return null;
        }
    }

    public SpotifyPlaylist fetchAlbum(String url) {
        try {
            List<SpotifySong> songs = new ArrayList<>();
            CompletableFuture<Album> albumFuture = spotify.getAlbum(getID(url)).build().executeAsync();
            Album album = albumFuture.get();
            for (TrackSimplified track : album.getTracks().getItems())
                songs.add(new SpotifySong(track.getName(), track.getArtists(), uriToUrl(track.getUri())));

            return new SpotifyPlaylist(album.getName(), songs.toArray(SpotifySong[]::new));
        } catch (Exception e) {
            log.error("Error when fetching spotify album `{}`: {}", getID(url), e.getMessage());
            return null;
        }
    }

    private String uriToUrl(String uri) {
        return "https://open.spotify.com/track/" + uri.substring(uri.lastIndexOf(":") + 1);
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