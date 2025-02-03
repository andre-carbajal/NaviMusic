package net.andrecarbajal.naviMusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.dto.VideoInfo;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
import net.andrecarbajal.naviMusic.util.URLUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class AudioResultHandler implements AudioLoadResultHandler {
    public AudioResultHandler(MusicService musicService, Guild guild, Member member) {
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
        String uri = track.getInfo().uri;

        RichResponse r = new RichResponse();

        r.setColor(new Color(0, 51, 102));
        r.setTitle("Song added to queue");
        r.setText(String.format("[%s](%s) de `%s`", track.getInfo().title.trim(), uri, track.getInfo().author));

        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field("Duration", new VideoInfo(track.getInfo()).durationToReadable(), true));

        int size = musicService.getGuildMusicManager(guild).getScheduler().getQueueSize()+1;
        fields.add(new MessageEmbed.Field("In queue", String.format(size==1?"%d song":"%d songs", size), true));

        r.setFields(fields);

        r.setFooter(new RichResponse.Footer(String.format("Added by %s", member.getEffectiveName()), member.getEffectiveAvatarUrl()));

        if (track.getSourceManager() instanceof YoutubeAudioSourceManager) {
            URLUtils.getURLParam(uri, "v").ifPresent(s -> r.setThumbnail(String.format("https://img.youtube.com/vi/%s/maxresdefault.jpg", s)));
        }

        response = r;
        musicService.play(guild, musicService.getGuildMusicManager(guild), track, member);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
            firstTrack = playlist.getTracks().getFirst();
        }

        if (playlist.isSearchResult()) {
            trackLoaded(firstTrack);
            return;
        }

        RichResponse r = new RichResponse();

        r.setColor(new Color(0, 51, 102));
        r.setTitle("Playlist added to queue");

        r.setText(String.format("%s", playlist.getName()));

        int playlistSize = playlist.getTracks().size();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field("Songs added", String.valueOf(playlistSize), true));

        int size = musicService.getGuildMusicManager(guild).getScheduler().getQueueSize()+playlistSize;
        fields.add(new MessageEmbed.Field("In queue", String.format(size==1?"%d song":"%d songs", size), true));

        r.setFields(fields);

        r.setFooter(new RichResponse.Footer(String.format("Added by %s", member.getEffectiveName()), member.getEffectiveAvatarUrl()));

        if (firstTrack.getSourceManager() instanceof YoutubeAudioSourceManager) {
            URLUtils.getURLParam(firstTrack.getInfo().uri, "v").ifPresent(s -> r.setThumbnail(String.format("https://img.youtube.com/vi/%s/maxresdefault.jpg", s)));
        }

        response = r;
        musicService.playPlaylist(guild, musicService.getGuildMusicManager(guild), playlist, member);
    }

    @Override
    public void noMatches() {
        RichResponse r = new RichResponse();

        r.setType(Response.Type.USER_ERROR);
        r.setText("Nothing found");
        response=r;
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        log.error("Error loading track", exception);

        RichResponse r = new RichResponse();
        r.setType(Response.Type.ERROR);
        r.setText("Internal error");
        response=r;
    }
}
