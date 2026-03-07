package net.andrecarbajal.naviMusic.commands.music

import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class ShuffleCommand(private val musicService: MusicService) :
    SlashCommand("shuffle", "Shuffles the queue", Category.MUSIC) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        if (noVoiceChannelCheck(event)) return
        event.deferReply().queue()

        val guild = event.guild ?: return
        musicService.getGuildMusicManager(guild).scheduler.shuffle()
        RichResponse("Queue shuffled", RichResponse.Type.OK, false).editReply(event)
    }
}
