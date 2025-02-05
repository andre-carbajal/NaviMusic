package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.GuildMusicManager;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class RepeatOffCommand extends SlashCommand {
    private final MusicService musicService;

    public RepeatOffCommand(MusicService musicService) {
        super("repeat-off", "Stop repeating current song");
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();

        GuildMusicManager guildMusicManager = musicService.getGuildMusicManager(event.getChannel().asTextChannel().getGuild());

        if (!guildMusicManager.scheduler.isRepeating())
            new Response("Not repeating", Response.Type.ERROR, false).editReply(event);
        else {
            guildMusicManager.scheduler.setRepeating(false);
            new Response("The song will not be repeated", Response.Type.OK, false).editReply(event);
        }
    }
}
