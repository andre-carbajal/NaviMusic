package net.andrecarbajal.naviMusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    @Getter
    private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
    @Getter
    @Setter
    private boolean repeating = false;

    public void queue(AudioTrack track) {
        log.info("Adding {} in the queue", track.getInfo().title);
        if (player.startTrack(track, true)) {
            logTackStarted(track);
        } else {
            if (!queue.offer(track)) throw new RuntimeException("Item was not added to the queue");
        }
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();
        if (track != null) {
            logTackStarted(track);
        }
        player.startTrack(track, false);
    }

    public void skipTrack(int position) {
        if (position < 1) return;

        if (position == 1) {
            nextTrack();
            return;
        }

        List<AudioTrack> tracks = new ArrayList<>(queue);
        tracks.remove(position - 1);
        queue.clear();
        queue.addAll(tracks);
    }

    public void clear() {
        queue.clear();
        nextTrack();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public void shuffle() {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        queue.clear();
        Collections.shuffle(tracks);
        tracks.forEach(this::queue);
    }

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (repeating) {
                player.startTrack(track.makeClone(), false);
                return;
            }
            nextTrack();
            return;
        }

        if (endReason == AudioTrackEndReason.LOAD_FAILED) {
            log.error("Error playing {} ({})", track.getInfo().title, track.getInfo().uri);
            nextTrack();
        }
    }

    private void logTackStarted(AudioTrack track) {
        Member member = track.getUserData(Member.class);

        if (member != null) {
            log.info("Playing {} in {} requested by {}", track.getInfo().title, member.getGuild().getName(), member.getEffectiveName());
        } else {
            log.info("Playing {}", track.getInfo().title);
        }
    }
}
