package net.andrecarbajal.naviMusic.audio.resultHandler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.audio.MusicService;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

@Slf4j
public class SpotifyResultHandler implements AudioLoadResultHandler {
    public SpotifyResultHandler(MusicService musicService, Guild guild, Member member) {
        this.musicService = musicService;
        this.guild = guild;
        this.member = member;
    }

    @Getter
    private Response response;

    private final MusicService musicService;
    private final Guild guild;
    private final Member member;

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

            r.setTitle("Adding spotify song to queue");
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
