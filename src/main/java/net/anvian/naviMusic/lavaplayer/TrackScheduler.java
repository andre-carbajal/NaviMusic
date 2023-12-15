package net.anvian.naviMusic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
    private boolean isRepeat = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(isRepeat) {
            player.startTrack(track.makeClone(), false);
        } else {
            player.startTrack(queue.poll(), false);
        }
    }

    public void queue(AudioTrack track) {
        if(!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void setQueue(List<AudioTrack> queue) {
        this.queue.clear();
        this.queue.addAll(queue);
    }

    public void removeSongAt(int index) {
        if (index >= 0 && index < queue.size()) {
            Iterator<AudioTrack> iterator = queue.iterator();
            int currentIndex = 0;
            while (iterator.hasNext()) {
                iterator.next();
                if (currentIndex == index) {
                    iterator.remove();
                    break;
                }
                currentIndex++;
            }
        }
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }
}