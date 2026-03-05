package net.andrecarbajal.naviMusic.commands.music

import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.dto.response.Response
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class RepeatCommand(private val musicService: MusicService) :
    SlashCommand("repeat", "Repeats the current track", Category.MUSIC) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        if (noVoiceChannelCheck(event)) return
        event.deferReply().queue()

        val guild = event.guild ?: return
        val guildMusicManager = musicService.getGuildMusicManager(guild)

        if (guildMusicManager.scheduler.isRepeating) {
            Response("Already repeating", Response.Type.ERROR, false).editReply(event)
        } else {
            guildMusicManager.scheduler.isRepeating = true
            Response("Repeating the current track", Response.Type.OK, false).editReply(event)
        }
    }
}
