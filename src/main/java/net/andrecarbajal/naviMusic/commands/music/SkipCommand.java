package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

@Component
public class SkipCommand extends SlashCommand {
    private final MusicService musicService;

    public SkipCommand(MusicService musicService) {
        super("skip", "Skip/remove first song from queue");
        addOption(new OptionData(OptionType.INTEGER, "position", "Position in queue to skip", false));
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        int position = event.getOption("position") == null ? 1 : (int) event.getOption("position").getAsLong();

        musicService.skipTrack(event.getChannel().asTextChannel(), position).sendReply(event);
    }
}
