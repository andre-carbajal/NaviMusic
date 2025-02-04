package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.andrecarbajal.naviMusic.audio.GuildMusicManager;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class SkipCommand extends SlashCommand {
    private final MusicService musicService;

    public SkipCommand(MusicService musicService) {
        super("skip", "Skip/remove first song from queue");
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        musicService.skipTrack(event.getChannel().asTextChannel()).sendReply(event);

        GuildMusicManager musicManager = musicService.getGuildMusicManager(event.getChannel().asTextChannel().getGuild());
        AudioTrack track = musicManager.getPlayer().getPlayingTrack();

        if (track != null) {
            new Response("Now playing: " + track.getInfo().title, Response.Type.OK, false);
        } else {
            new Response("No track playing", Response.Type.ERROR, false);
        }
    }
}
