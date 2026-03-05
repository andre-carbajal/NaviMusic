package net.andrecarbajal.naviMusic.commands.music

import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.util.URLUtils
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PlayCommand(private val musicService: MusicService) :
    SlashCommand("play", "Play track or playlist", Category.MUSIC) {

    private val log = LoggerFactory.getLogger(PlayCommand::class.java)

    init {
        addOption(OptionData(OptionType.STRING, "name-url-playlist", "Link or search query", true))
    }

    override fun onCommand(event: SlashCommandInteractionEvent) {
        if (noVoiceChannelCheck(event)) return
        event.deferReply().queue()

        try {
            val query = event.getOption("name-url-playlist")?.asString ?: return
            val provider = "ytsearch"
            val textChannel = event.channel.asTextChannel()
            val member = event.member

            if (URLUtils.isURL(query)) {
                if (query.contains("spotify", ignoreCase = true)) {
                    musicService.loadAndPlaySpotifyUrl(textChannel, provider, query, member).editReply(event)
                } else {
                    musicService.loadAndPlayUrl(textChannel, query, member).editReply(event)
                }
            } else {
                musicService.loadAndPlay(textChannel, provider, query, member).editReply(event)
            }
        } catch (e: Exception) {
            log.error("Error in PlayCommand", e)
            throw RuntimeException(e)
        }
    }
}
