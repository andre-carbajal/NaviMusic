package net.andrecarbajal.naviMusic.commands.music

import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class ClearCommand(private val musicService: MusicService) : SlashCommand("clear", "Clear the queue", Category.MUSIC) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        if (noVoiceChannelCheck(event)) return
        val guild = event.guild ?: return

        musicService.clear(guild).sendReply(event)
    }
}
