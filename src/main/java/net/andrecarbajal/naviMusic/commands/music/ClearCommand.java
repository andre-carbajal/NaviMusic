package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class ClearCommand extends SlashCommand {
    private final MusicService musicService;
    public ClearCommand(MusicService musicService) {
        super("clear", "Limpia la lista de reproducción");

        this.musicService=musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;

        musicService.clear(event.getGuild()).sendReply(event);
    }
}

