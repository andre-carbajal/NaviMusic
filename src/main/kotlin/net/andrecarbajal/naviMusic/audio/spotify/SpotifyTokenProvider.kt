package net.andrecarbajal.naviMusic.audio.spotify

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import se.michaelthelin.spotify.SpotifyApi

@Component
class SpotifyTokenProvider(
    @Value("\${app.spotify.clientId}") clientId: String,
    @Value("\${app.spotify.clientSecret}") clientSecret: String
) {
    private val log = LoggerFactory.getLogger(SpotifyTokenProvider::class.java)

    private val spotifyApi: SpotifyApi = SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .build()

    private var accessToken: String? = null
    private var tokenExpirationTime: Long = 0

    init {
        refreshAccessToken()
    }

    @Synchronized
    fun getAccessToken(): String? {
        if (System.currentTimeMillis() >= tokenExpirationTime) {
            refreshAccessToken()
        }
        return accessToken
    }

    fun getSpotifyApi(): SpotifyApi {
        spotifyApi.accessToken = getAccessToken()
        return spotifyApi
    }

    private fun refreshAccessToken() {
        try {
            val clientCredentialsRequest = spotifyApi.clientCredentials().build()
            val clientCredentials = clientCredentialsRequest.execute()
            this.accessToken = clientCredentials.accessToken
            this.tokenExpirationTime = System.currentTimeMillis() + (clientCredentials.expiresIn - 60) * 1000L
            spotifyApi.accessToken = this.accessToken
        } catch (e: Exception) {
            log.error("Error refreshing Spotify access token: {}", e.message)
        }
    }
}
