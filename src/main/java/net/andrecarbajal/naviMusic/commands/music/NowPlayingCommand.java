package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class NowPlayingCommand extends SlashCommand {
    private final MusicService musicService;

    public NowPlayingCommand(MusicService musicService) {
        super("nowplaying", "Will display the current playing song");
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;

        musicService.nowPlaying(event.getChannel().asTextChannel()).sendReply(event);
    }
}
