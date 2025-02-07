package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.andrecarbajal.naviMusic.audio.GuildMusicManager;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.VideoInfo;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
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

    private Response response;

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();

        GuildMusicManager guildMusicManager = musicManager.getGuildMusicManager(event.getChannel().asTextChannel().getGuild());
        List<AudioTrack> queue = guildMusicManager.getScheduler().getQueue().stream().toList();

        if (queue.isEmpty()) {
            if (guildMusicManager.player.getPlayingTrack() == null) {
                new Response("No song in queue", Response.Type.OK, false).editReply(event);
                return;
            }


            AudioTrackInfo current = guildMusicManager.player.getPlayingTrack().getInfo();
            RichResponse.builder().title("Currently playing").text(current.title + " (" + new VideoInfo(current).durationToReadable() + ")").build().editReply(event);
            return;
        }

        StringBuilder builder = new StringBuilder();
        long totalDuration = queue.stream().mapToLong(AudioTrack::getDuration).sum();
        int totalSongs = queue.size();

        for (int i = 0; i < Math.min(15, totalSongs); i++) {
            AudioTrack track = queue.get(i);
            AudioTrackInfo info = track.getInfo();
            builder.append(String.format("%d. %s (%s)\n", i + 1, info.title, new VideoInfo(info).durationToReadable()));
        }

        if (totalSongs > 15) {
            builder.append("\nAnd ").append(totalSongs - 15).append(" more...");
        }

        builder.append(String.format("\nTotal songs: %d\n", totalSongs));
        builder.append(String.format("Total duration: %s", new VideoInfo(totalDuration).durationToReadableFromDuration()));

        RichResponse.builder().title("Queue").text(builder.toString()).build().editReply(event);
    }
}
