package net.andrecarbajal.naviMusic.audio.spotify

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import se.michaelthelin.spotify.SpotifyApi

@Component
class SpotifyTokenProvider(
    @Value($$"${app.spotify.clientId}") private val clientId: String,
    @Value($$"${app.spotify.clientSecret}") private val clientSecret: String
) {
    private val log = LoggerFactory.getLogger(SpotifyTokenProvider::class.java)

    private val spotifyApi: SpotifyApi = SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .build()

    private var accessToken: String? = null
    private var tokenExpirationTime: Long = 0

    init {
        if (isConfigured()) {
            refreshAccessToken()
        } else {
            log.warn("Spotify API is not configured. Spotify links will not work.")
        }
    }

    fun isConfigured(): Boolean {
        return clientId.isNotBlank() && clientId != "default" && 
               clientSecret.isNotBlank() && clientSecret != "default"
    }

    @Synchronized
    fun getAccessToken(): String? {
        if (!isConfigured()) return null

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
        if (!isConfigured()) return
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
