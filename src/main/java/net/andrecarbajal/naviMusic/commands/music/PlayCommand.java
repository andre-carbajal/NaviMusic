package net.andrecarbajal.naviMusic.commands.music;

import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.util.URLUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlayCommand extends SlashCommand {
    private final MusicService musicService;

    public PlayCommand(MusicService musicService) {
        super("play", "Play track or playlist", Category.MUSIC);
        addOption(new OptionData(OptionType.STRING, "name-url-playlist", "Link or search query", true));
        this.musicService = musicService;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (noVoiceChannelCheck(event)) return;
        event.deferReply().queue();
        try {
            String str = event.getOption("name-url-playlist", OptionMapping::getAsString);

            assert str != null;

            String PROVIDER = "ytsearch";
            
            if (URLUtils.isURL(str)) {
                if (str.toUpperCase().contains("SPOTIFY")) {
                    musicService.loadAndPlaySpotifyUrl(event.getChannel().asTextChannel(), PROVIDER, str, event.getMember()).editReply(event);
                    return;
                }
                musicService.loadAndPlayUrl(event.getChannel().asTextChannel(), str, event.getMember()).editReply(event);
            } else {
                musicService.loadAndPlay(event.getChannel().asTextChannel(), PROVIDER, str, event.getMember()).editReply(event);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

