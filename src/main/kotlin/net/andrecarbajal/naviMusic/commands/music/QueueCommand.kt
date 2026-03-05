package net.andrecarbajal.naviMusic.commands.music

import net.andrecarbajal.naviMusic.audio.MusicService
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.dto.VideoInfo
import net.andrecarbajal.naviMusic.dto.response.Response
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class QueueCommand(private val musicManager: MusicService) :
    SlashCommand("queue", "Display list of songs in queue", Category.MUSIC) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        if (noVoiceChannelCheck(event)) return
        event.deferReply().queue()

        val guild = event.guild ?: return
        val guildMusicManager = musicManager.getGuildMusicManager(guild)
        val queue = guildMusicManager.scheduler.queue.toList()

        if (queue.isEmpty()) {
            val playingTrack = guildMusicManager.player.playingTrack
            if (playingTrack == null) {
                Response("No song in queue", Response.Type.OK, false).editReply(event)
                return
            }

            val info = playingTrack.info
            RichResponse.builder()
                .title("Currently playing")
                .text("${info.title} (${VideoInfo(info).durationToReadable()})")
                .build()
                .editReply(event)
            return
        }

        val totalSongs = queue.size
        val totalDuration = queue.sumOf { it.duration }
        val builder = StringBuilder()

        for (i in 0 until Math.min(15, totalSongs)) {
            val info = queue[i].info
            builder.append("${i + 1}. ${info.title} `[${VideoInfo(info).durationToReadable()}]`\n")
        }

        if (totalSongs > 15) {
            builder.append("And ${totalSongs - 15} more...")
        }

        RichResponse.builder()
            .title("Queue")
            .text(builder.toString())
            .fields(
                listOf(
                    MessageEmbed.Field("Total songs", totalSongs.toString(), true),
                    MessageEmbed.Field(
                        "Total duration",
                        VideoInfo(totalDuration).durationToReadableFromDuration(),
                        true
                    )
                )
            )
            .build()
            .editReply(event)
    }
}
