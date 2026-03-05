package net.andrecarbajal.naviMusic.commands.music

import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.dto.response.Response
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class LeaveCommand(private val musicManager: MusicService) :
    SlashCommand("leave", "Will make the bot leave the voice channel", Category.MUSIC) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        if (noVoiceChannelCheck(event)) return

        val guild = event.guild ?: return
        val guildMusicManager = musicManager.getGuildMusicManager(guild)
        guildMusicManager.scheduler.clear()
        guildMusicManager.player.stopTrack()

        val audioManager = guild.audioManager
        val connectedChannelName = audioManager.connectedChannel?.name ?: "unknown"
        audioManager.closeAudioConnection()

        Response("Leaving the voice channel: $connectedChannelName", Response.Type.OK, false).sendReply(event)
    }
}
