package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class ShuffleCommand extends SlashCommand {
    private final MusicService musicService;

    public ShuffleCommand(MusicService musicService) {
        super("shuffle", "Shuffles the queue", Category.MUSIC);
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();

        musicService.getGuildMusicManager(event.getGuild()).getScheduler().shuffle();
        new Response("Queue shuffled", Response.Type.OK, false).editReply(event);
    }
}

