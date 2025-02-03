package net.andrecarbajal.naviMusic.dto;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class VideoInfo {
    private final AudioTrackInfo info;

    public String durationToReadable() {
        long hours = TimeUnit.MILLISECONDS.toHours(info.length);
        long minute = TimeUnit.MILLISECONDS.toMinutes(info.length) - TimeUnit.HOURS.toMinutes(hours);
        long second = TimeUnit.MILLISECONDS.toSeconds(info.length) - TimeUnit.MINUTES.toSeconds(minute);

        return String.format("%d:%02d:%02d", hours, minute, second);
    }
}
