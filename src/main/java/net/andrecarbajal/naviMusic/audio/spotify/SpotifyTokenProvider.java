package net.andrecarbajal.naviMusic.audio.spotify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

@Slf4j
@Component
public class SpotifyTokenProvider {
    private final SpotifyApi spotifyApi;
    private String accessToken;
    private long tokenExpirationTime = 0;

    public SpotifyTokenProvider(@Value("${app.spotify.clientId}") String clientId,
                                @Value("${app.spotify.clientSecret}") String clientSecret) {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        refreshAccessToken();
    }

    public synchronized String getAccessToken() {
        if (System.currentTimeMillis() >= tokenExpirationTime) {
            refreshAccessToken();
        }
        return accessToken;
    }

    public SpotifyApi getSpotifyApi() {
        spotifyApi.setAccessToken(getAccessToken());
        return spotifyApi;
    }

    private void refreshAccessToken() {
        try {
            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            this.accessToken = clientCredentials.getAccessToken();
            this.tokenExpirationTime = System.currentTimeMillis() + (clientCredentials.getExpiresIn() - 60) * 1000L; // 60s margen
            spotifyApi.setAccessToken(this.accessToken);
        } catch (Exception e) {
            log.error("Error refreshing Spotify access token: {}", e.getMessage());
        }
    }
}

