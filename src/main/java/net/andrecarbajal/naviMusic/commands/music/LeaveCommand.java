package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.GuildMusicManager;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class LeaveCommand extends SlashCommand {
    private final MusicService musicManager;

    public LeaveCommand(MusicService musicManager) {
        super("leave", "Will make the bot leave the voice channel", Category.MUSIC);
        this.musicManager = musicManager;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;

        GuildMusicManager guildMusicManager = musicManager.getGuildMusicManager(event.getChannel().asTextChannel().getGuild());
        guildMusicManager.getScheduler().clear();
        guildMusicManager.getPlayer().stopTrack();

        event.getGuild().getAudioManager().closeAudioConnection();

        new Response(String.format("Leaving the voice channel: %s", event.getGuild().getAudioManager().getConnectedChannel().getName()), Response.Type.OK, false).sendReply(event);
    }
}
