package net.andrecarbajal.naviMusic.config

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.*
import dev.lavalink.youtube.clients.skeleton.Client
import net.andrecarbajal.naviMusic.audio.MusicService
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AudioConfig(private val musicService: MusicService) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(AudioConfig::class.java)

    @Value("\${app.youtube.potoken}")
    private lateinit var poToken: String

    @Value("\${app.youtube.visitor}")
    private lateinit var visitorData: String

    @Value("\${app.youtube.oauth2}")
    private lateinit var oAuthToken: String

    @Bean
    fun setupAudioSources(): AudioPlayerManager {
        val playerManager = DefaultAudioPlayerManager()

        if (poToken.isNotBlank() && poToken != "null") {
            Web.setPoTokenAndVisitorData(poToken, visitorData)
        }

        val useOauth = oAuthToken.isNotBlank() && oAuthToken != "null"

        val clients = mutableListOf<Client>()

        if (useOauth) {
            clients.add(AndroidVrWithThumbnail())
            clients.add(AndroidMusicWithThumbnail())
            clients.add(TvHtml5SimplyWithThumbnail())
            clients.add(IosWithThumbnail())
            clients.add(WebWithThumbnail())
        } else {
            clients.add(AndroidVrWithThumbnail())
            clients.add(MusicWithThumbnail())
            clients.add(WebWithThumbnail())
            clients.add(TvHtml5SimplyWithThumbnail())
            clients.add(AndroidMusicWithThumbnail())
            clients.add(IosWithThumbnail())
        }

        val youtube = YoutubeAudioSourceManager(true, *clients.toTypedArray())

        if (useOauth) {
            youtube.useOauth2(oAuthToken, true)
            log.info("YouTube OAuth enabled with {} clients (Priority: ANDROID_VR)", clients.size)
        }

        playerManager.registerSourceManager(youtube)

        AudioSourceManagers.registerRemoteSources(
            playerManager,
            com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager::class.java
        )

        return playerManager
    }

    override fun onGuildVoiceUpdate(e: GuildVoiceUpdateEvent) {
        val selfMember = e.guild.selfMember
        val voiceState = selfMember.voiceState ?: return
        val connectedChannel = voiceState.channel ?: return

        if (connectedChannel.members.size == 1) {
            log.info("Bot is alone. Disconnecting and clearing queue.")
            musicService.getGuildMusicManager(e.guild).scheduler.clear()
            e.guild.audioManager.closeAudioConnection()
        }
    }
}
