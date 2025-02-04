package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class PauseCommand extends SlashCommand {
    private final MusicService musicService;

    public PauseCommand(MusicService musicService) {
        super("pause", "Pauses music");
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;

        musicService.pausePlaying(event.getChannel().asTextChannel()).sendReply(event);
    }
}
