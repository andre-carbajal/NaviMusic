package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class ResumeCommand extends SlashCommand {
    private final MusicService musicService;

    public ResumeCommand(MusicService musicService) {
        super("resume", "Resumes music");
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();

        musicService.resume(event.getChannel().asTextChannel()).editReply(event);
    }
}
