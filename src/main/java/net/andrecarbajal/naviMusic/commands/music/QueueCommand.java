package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.andrecarbajal.naviMusic.audio.GuildMusicManager;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.VideoInfo;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueueCommand extends SlashCommand {
    private final MusicService musicManager;

    public QueueCommand(MusicService musicManager) {
        super("queue", "Display list of songs in queue", Category.MUSIC);
        this.musicManager = musicManager;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();

        GuildMusicManager guildMusicManager = musicManager.getGuildMusicManager(event.getChannel().asTextChannel().getGuild());
        List<AudioTrack> queue = guildMusicManager.getScheduler().getQueue().stream().toList();

        if (queue.isEmpty()) {
            new Response("Queue is empty", Response.Type.OK, false).editReply(event);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < queue.size(); i++) {
            AudioTrack track = queue.get(i);
            String duration = new VideoInfo(track.getInfo()).durationToReadable();
            builder.append(i + 1).append(". ").append(track.getInfo().title).append(" (").append(duration).append(")\n");
        }

        new Response(builder.toString(), Response.Type.OK, false).editReply(event);
    }
}
