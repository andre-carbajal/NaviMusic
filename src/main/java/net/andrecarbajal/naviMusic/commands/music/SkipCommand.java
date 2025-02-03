package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class SkipCommand extends SlashCommand {
    private final MusicService musicService;

    public SkipCommand(MusicService musicService) {
        super("skip", "Skip/remove first song from queue");

        this.musicService=musicService;
    }
    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        musicService.skipTrack(event.getChannel().asTextChannel()).sendReply(event);
    }
}
