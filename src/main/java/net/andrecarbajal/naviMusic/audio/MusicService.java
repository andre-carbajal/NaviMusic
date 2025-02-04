package net.andrecarbajal.naviMusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class MusicService {
    private final AudioPlayerManager audioManager;

    private final Map<Long, GuildMusicManager> managers = new HashMap<>();

    public MusicService(@Lazy AudioPlayerManager audioManager) {
        this.audioManager = audioManager;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        GuildMusicManager musicManager = managers.computeIfAbsent(guild.getIdLong(), a -> new GuildMusicManager(audioManager));

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public Response loadAndPlay(final TextChannel channel, final String trackUrl, Member member) throws ExecutionException, InterruptedException {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        AudioResultHandler audioResult = new AudioResultHandler(this, channel.getGuild(), member);
        audioManager.loadItemOrdered(musicManager, trackUrl, audioResult).get();

        return audioResult.getResponse();
    }

    public Response skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        musicManager.getScheduler().nextTrack();

        return new Response("Skipped", Response.Type.OK, false);
    }

    public Response clear(Guild guild) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);
        musicManager.getScheduler().clear();

        return new Response("Cleaning...", Response.Type.OK, false);
    }

    public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, Member member) {
        connectToChannel(guild.getAudioManager(), member);
        track.setUserData(member);
        musicManager.getScheduler().queue(track);
    }

    public void playPlaylist(Guild guild, GuildMusicManager musicManager, AudioPlaylist playlist, Member member) {
        connectToChannel(guild.getAudioManager(), member);

        for (AudioTrack track : playlist.getTracks()) {
            track.setUserData(member);
            musicManager.getScheduler().queue(track);
        }

    }

    public boolean connectToChannel(AudioManager audioManager, Member member) {
        if (audioManager.isConnected()) return false;

        try {
            audioManager.openAudioConnection(audioManager.getGuild().getVoiceChannels().stream().filter(voiceChannel -> voiceChannel.getMembers().contains(member)).findFirst().orElseThrow());
            return true;
        } catch (Exception ex) {
            log.error("Error joining voice channel", ex);
            return false;
        }

    }

    public Response nowPlaying(TextChannel textChannel) {
        GuildMusicManager musicManager = getGuildMusicManager(textChannel.getGuild());
        AudioTrack track = musicManager.getPlayer().getPlayingTrack();

        if (track == null) {
            return new Response("No track playing", Response.Type.ERROR, false);
        }

        return new Response("Now playing: " + track.getInfo().title, Response.Type.OK, false);
    }

    public Response pausePlaying(TextChannel textChannel) {
        GuildMusicManager musicManager = getGuildMusicManager(textChannel.getGuild());
        AudioPlayer player = musicManager.getPlayer();
        if (player.isPaused()) return new Response("Already paused", Response.Type.ERROR, false);

        player.setPaused(true);
        return new Response("Paused", Response.Type.OK, false);
    }

    public Response continuePlaying(TextChannel textChannel) {
        GuildMusicManager musicManager = getGuildMusicManager(textChannel.getGuild());
        AudioPlayer player = musicManager.getPlayer();
        if (!player.isPaused()) return new Response("Already playing", Response.Type.ERROR, false);

        player.setPaused(false);
        return new Response("Continuing", Response.Type.OK, false);
    }
}
