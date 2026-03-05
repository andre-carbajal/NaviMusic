package net.andrecarbajal.naviMusic.commands.music

import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component

@Component
class SkipCommand(private val musicService: MusicService) :
    SlashCommand("skip", "Skip/remove first song from queue", Category.MUSIC) {

    init {
        addOption(OptionData(OptionType.INTEGER, "position", "Position in queue to skip", false))
    }

    override fun onCommand(event: SlashCommandInteractionEvent) {
        if (noVoiceChannelCheck(event)) return

        val position = event.getOption("position")?.asLong?.toInt() ?: 1
        val textChannel = event.channel.asTextChannel()
        musicService.skipTrack(textChannel, position).sendReply(event)
    }
}
