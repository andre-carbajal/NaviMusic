package net.andrecarbajal.naviMusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.audio.resultHandler.AudioResultHandler;
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyPlaylistResultHandler;
import net.andrecarbajal.naviMusic.audio.resultHandler.SpotifyTrackResultHandler;
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyFetch;
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyPlaylist;
import net.andrecarbajal.naviMusic.audio.spotify.SpotifySong;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class MusicService {
    private final AudioPlayerManager audioManager;
    private final SpotifyFetch spotifyFetch;

    private final Map<Long, GuildMusicManager> managers = new HashMap<>();

    public MusicService(@Lazy AudioPlayerManager audioManager, SpotifyFetch spotifyFetch) {
        this.audioManager = audioManager;
        this.spotifyFetch = spotifyFetch;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        GuildMusicManager musicManager = managers.computeIfAbsent(guild.getIdLong(), a -> new GuildMusicManager(audioManager, guild));

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public Response loadAndPlay(final TextChannel channel, final String provider, final String track, Member member) throws ExecutionException, InterruptedException {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        AudioResultHandler audioResult = new AudioResultHandler(this, channel.getGuild(), member);
        audioManager.loadItemOrdered(musicManager, String.format("%s: %s", provider, track), audioResult).get();

        return audioResult.getResponse();
    }

    public Response loadAndPlayUrl(final TextChannel channel, final String trackUrl, Member member) throws ExecutionException, InterruptedException {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        AudioResultHandler audioResult = new AudioResultHandler(this, channel.getGuild(), member);
        audioManager.loadItemOrdered(musicManager, trackUrl, audioResult).get();

        return audioResult.getResponse();
    }

    public Response loadAndPlaySpotifyUrl(TextChannel channel, final String provider, final String trackUrl, Member member) throws ExecutionException, InterruptedException {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        if (trackUrl.toUpperCase().contains("track".toUpperCase())) {
            SpotifySong song = spotifyFetch.fetchSong(trackUrl);

            if (song == null) return new Response("Error fetching spotify song", Response.Type.ERROR, false);

            SpotifyTrackResultHandler spotifyTrackResultHandler = new SpotifyTrackResultHandler(this, channel.getGuild(), member, song);
            audioManager.loadItemOrdered(musicManager, String.format("%s: %s", provider, song), spotifyTrackResultHandler).get();

            return spotifyTrackResultHandler.getResponse();
        }

        if (trackUrl.toUpperCase().contains("playlist".toUpperCase())) {
            SpotifyPlaylist playlist = spotifyFetch.fetchPlaylist(trackUrl);
            loadSpotifySongs(playlist, channel, member, provider);
            return spotifyResponse(this, channel.getGuild(), member, playlist, "playlist");
        }

        if (trackUrl.toUpperCase().contains("album".toUpperCase())) {
            SpotifyPlaylist playlist = spotifyFetch.fetchAlbum(trackUrl);
            loadSpotifySongs(playlist, channel, member, provider);
            return spotifyResponse(this, channel.getGuild(), member, playlist, "album");
        }
        return new Response("Couldn't find spotify link", Response.Type.ERROR, false);
    }

    private Response spotifyResponse(MusicService musicService, Guild guild, Member member, SpotifyPlaylist playlist, String type) {
        RichResponse r = new RichResponse();

        r.setTitle(String.format("Adding Spotify %s to queue", type));
        r.setText(playlist.title());

        int playlistSize = playlist.songs().length;
        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field("Songs", String.format("%d songs", playlistSize), true));

        int size = musicService.getGuildMusicManager(guild).getScheduler().getQueueSize() + playlistSize;
        fields.add(new MessageEmbed.Field("In queue", String.format(size == 1 ? "%d song" : "%d songs", size), true));

        r.setFields(fields);

        r.setFooter(new RichResponse.Footer(String.format("Added by %s", member.getEffectiveName()), member.getEffectiveAvatarUrl()));

        return r;
    }

    private void loadSpotifySongs(SpotifyPlaylist playlist, TextChannel channel, Member member, String provider) {
        for (SpotifySong song : playlist.songs()) {
            log.warn("Loading song: {}", song.toString());
            GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
            SpotifyPlaylistResultHandler spotifyPlaylistResultHandler = new SpotifyPlaylistResultHandler(this, channel.getGuild(), member);
            audioManager.loadItemOrdered(musicManager, String.format("%s: %s", provider, song), spotifyPlaylistResultHandler);
        }
    }

    public Response skipTrack(TextChannel channel, int position) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        if (position < 1) return new Response("Invalid position", Response.Type.ERROR, false);

        if (position == 1) {
            musicManager.getScheduler().nextTrack();
            return new Response("Skipped", Response.Type.OK, false);
        }

        musicManager.getScheduler().skipTrack(position - 1);

        return new Response(String.format("Skipping song %d", position), Response.Type.OK, false);
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

    public Response pause(TextChannel textChannel) {
        GuildMusicManager musicManager = getGuildMusicManager(textChannel.getGuild());
        AudioPlayer player = musicManager.getPlayer();
        if (player.isPaused()) return new Response("Already paused", Response.Type.ERROR, false);

        player.setPaused(true);
        return new Response("Paused", Response.Type.OK, false);
    }

    public Response resume(TextChannel textChannel) {
        GuildMusicManager musicManager = getGuildMusicManager(textChannel.getGuild());
        AudioPlayer player = musicManager.getPlayer();
        if (!player.isPaused()) return new Response("Already playing", Response.Type.ERROR, false);

        player.setPaused(false);
        return new Response("Resumed ", Response.Type.OK, false);
    }
}
