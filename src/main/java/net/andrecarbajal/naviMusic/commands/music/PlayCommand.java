package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class PlayCommand extends SlashCommand {
    private final MusicService musicService;

    public PlayCommand(MusicService musicService) {
        super("play", "Agrega un video (o playlist) a la cola");
        addOption(new OptionData(OptionType.STRING, "canción", "URL o nombre de la canción", true));

        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();
        try {
            String cancion = event.getOption("canción", OptionMapping::getAsString);
            try {
                new URI(cancion);
            } catch (URISyntaxException e) {
                cancion = "ytsearch:" + cancion;
            }
            musicService.loadAndPlay(event.getChannel().asTextChannel(), cancion, event.getMember()).editReply(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

