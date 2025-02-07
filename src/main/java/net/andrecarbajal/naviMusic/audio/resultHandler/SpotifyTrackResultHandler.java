package net.andrecarbajal.naviMusic.audio.resultHandler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.audio.spotify.SpotifySong;
import net.andrecarbajal.naviMusic.dto.VideoInfo;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SpotifyTrackResultHandler implements AudioLoadResultHandler {
    public SpotifyTrackResultHandler(MusicService musicService, Guild guild, Member member, SpotifySong song) {
        this.musicService = musicService;
        this.guild = guild;
        this.member = member;
        this.song = song;
    }

    @Getter
    private Response response;

    private final MusicService musicService;
    private final Guild guild;
    private final Member member;
    private final SpotifySong song;

    @Override
    public void trackLoaded(AudioTrack track) {
        RichResponse r = new RichResponse();
        r.setTitle("Not Spotify URL");
        response = r;
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            RichResponse r = new RichResponse();

            r.setTitle("Adding Spotify song to queue");
            r.setText(String.format("[%s](%s) by `%s`", song.title(), song.url(), song.getArtists()));

            List<MessageEmbed.Field> fields = new ArrayList<>();
            fields.add(new MessageEmbed.Field("Duration", new VideoInfo(playlist.getTracks().getFirst().getInfo()).durationToReadable(), true));

            int size = musicService.getGuildMusicManager(guild).getScheduler().getQueueSize() + 1;
            fields.add(new MessageEmbed.Field("In queue", String.format(size == 1 ? "%d song" : "%d songs", size), true));

            r.setFields(fields);

            r.setFooter(new RichResponse.Footer(String.format("Added by %s", member.getEffectiveName()), member.getEffectiveAvatarUrl()));

            response = r;
            musicService.play(guild, musicService.getGuildMusicManager(guild), playlist.getTracks().getFirst(), member);
        }
    }

    @Override
    public void noMatches() {
        RichResponse r = new RichResponse();

        r.setType(Response.Type.USER_ERROR);
        r.setText("Nothing found");
        response = r;
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        log.error("Error loading spotify track", exception);

        RichResponse r = new RichResponse();
        r.setType(Response.Type.ERROR);
        r.setText("Internal error");
        response = r;
    }
}
