package net.andrecarbajal.naviMusic.dto;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class VideoInfo {
    private AudioTrackInfo info;
    private long duration;

    public VideoInfo(AudioTrackInfo info) {
        this.info = info;
        this.duration = info.length;
    }

    public VideoInfo(long duration) {
        this.duration = duration;
    }

    public String durationToReadable() {
        long hours = TimeUnit.MILLISECONDS.toHours(info.length);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(info.length) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(info.length) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));

        return (hours > 0) ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public String durationToReadableFromDuration() {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));

        return (hours > 0) ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }
}
