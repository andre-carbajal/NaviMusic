package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.GuildMusicManager;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class RepeatCommand extends SlashCommand {
    private final MusicService musicService;

    public RepeatCommand(MusicService musicService) {
        super("repeat", "Repeats the current track");
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();

        GuildMusicManager guildMusicManager = musicService.getGuildMusicManager(event.getChannel().asTextChannel().getGuild());

        if (guildMusicManager.scheduler.isRepeating())
            new Response("Already repeating", Response.Type.ERROR, false).editReply(event);
        else {
            guildMusicManager.scheduler.setRepeating(true);
            new Response("Repeating the current track", Response.Type.OK, false).editReply(event);
        }
    }
}
