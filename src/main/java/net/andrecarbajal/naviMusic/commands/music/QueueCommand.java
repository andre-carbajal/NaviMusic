package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.andrecarbajal.naviMusic.audio.GuildMusicManager;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.VideoInfo;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
            builder.append(String.format("%d. %s `[%s]`\n", i + 1, info.title, new VideoInfo(info).durationToReadable()));
        }

        if (totalSongs > 15) {
            builder.append("And ").append(totalSongs - 15).append(" more...");
        }

        RichResponse.builder().title("Queue").text(builder.toString()).fields(
                List.of(new MessageEmbed.Field("Total songs", String.valueOf(totalSongs), true),
                        new MessageEmbed.Field("Total duration", new VideoInfo(totalDuration).durationToReadableFromDuration(), true))
        ).build().editReply(event);
    }
}
