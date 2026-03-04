package net.andrecarbajal.naviMusic.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.AndroidMusicWithThumbnail;
import dev.lavalink.youtube.clients.AndroidVrWithThumbnail;
import dev.lavalink.youtube.clients.IosWithThumbnail;
import dev.lavalink.youtube.clients.MusicWithThumbnail;
import dev.lavalink.youtube.clients.TvHtml5SimplyWithThumbnail;
import dev.lavalink.youtube.clients.Web;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import dev.lavalink.youtube.clients.skeleton.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AudioConfig extends ListenerAdapter {

    private final MusicService musicService;

    @Value("${app.youtube.potoken}")
    private String poToken;

    @Value("${app.youtube.visitor}")
    private String visitorData;

    @Value("${app.youtube.oauth2}")
    private String oAuthToken;

    @Bean
    public AudioPlayerManager setupAudioSources() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        if (poToken != null && !poToken.isEmpty() && !poToken.equals("null")) {
            Web.setPoTokenAndVisitorData(poToken, visitorData);
        }

        boolean useOauth = oAuthToken != null && !oAuthToken.equals("null") && !oAuthToken.isEmpty();

        List<Client> clients = new ArrayList<>();

        if (useOauth) {
            clients.add(new AndroidVrWithThumbnail());
            clients.add(new AndroidMusicWithThumbnail());
            clients.add(new TvHtml5SimplyWithThumbnail());
            clients.add(new IosWithThumbnail());
            clients.add(new WebWithThumbnail());
        } else {
            clients.add(new AndroidVrWithThumbnail());
            clients.add(new MusicWithThumbnail());
            clients.add(new WebWithThumbnail());
            clients.add(new TvHtml5SimplyWithThumbnail());
            clients.add(new AndroidMusicWithThumbnail());
            clients.add(new IosWithThumbnail());
        }

        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true, clients.toArray(new Client[0]));

        if (useOauth) {
            youtube.useOauth2(oAuthToken, true);
            log.info("YouTube OAuth enabled with {} clients (Priority: ANDROID_VR)", clients.size());
        }

        playerManager.registerSourceManager(youtube);

        AudioSourceManagers.registerRemoteSources(playerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);

        return playerManager;
    }


    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent e) {
        if (e.getChannelLeft() == null) return;

        if (e.getMember().equals(e.getGuild().getSelfMember())) {
            musicService.getGuildMusicManager(e.getGuild()).getScheduler().clear();
            return;
        }

        GuildVoiceState state = e.getGuild().getSelfMember().getVoiceState();
        assert state != null;
        if (state.getChannel() == null) return;

        AudioChannelUnion channel = state.getChannel();
        if (channel.getId().equals(e.getChannelLeft().getId())) {
            if (channel.getMembers().size() == 1) {
                musicService.getGuildMusicManager(e.getGuild()).getScheduler().clear();
                e.getGuild().getAudioManager().closeAudioConnection();
            }
        }

    }
}
