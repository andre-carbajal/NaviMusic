package net.andrecarbajal.naviMusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TrackScheduler extends AudioEventAdapter {
    private final List<AudioTrack> queue;
    private final AudioPlayer player;
    private boolean repeat = false;

    public TrackScheduler(AudioPlayer player) {
        queue = Collections.synchronizedList(new LinkedList<>());
        this.player = player;
    }

    public List<AudioTrack> getQueue() {
        return queue;
    }

    public boolean play(AudioTrack track) {
        return play(track, false);
    }

    public boolean play(AudioTrack track, boolean force) {
        boolean playing = player.startTrack(track, !force);
        if (!playing)
            queue.add(track);
        return playing;
    }

    public boolean skip() {
        return !queue.isEmpty() && play(queue.removeFirst(), true);
    }

    public boolean skip(int index){
        if (!queue.isEmpty()){
            queue.remove(index);
            return true;
        }
        return false;
    }

    public void clear() {
        this.queue.clear();
    }

    public void shuffle() {
        Collections.shuffle(queue);
    }

    public void setRepeating(boolean repeating){
        this.repeat = repeating;
    }

    public boolean isRepeating(){
        return this.repeat;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (repeat)
                player.startTrack(track.makeClone(), false);
            else
                skip();
        }
    }
}
