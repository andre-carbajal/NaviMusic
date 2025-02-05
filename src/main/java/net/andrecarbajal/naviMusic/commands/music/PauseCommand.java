package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class PauseCommand extends SlashCommand {
    private final MusicService musicService;

    public PauseCommand(MusicService musicService) {
        super("pause", "Pauses music", Category.MUSIC);
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();

        musicService.pause(event.getChannel().asTextChannel()).editReply(event);
    }
}
