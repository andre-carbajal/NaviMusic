package net.andrecarbajal.naviMusic.dto

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import java.util.concurrent.TimeUnit

class VideoInfo {
    var info: AudioTrackInfo? = null
    var duration: Long = 0

    constructor(info: AudioTrackInfo) {
        this.info = info
        this.duration = info.length
    }

    constructor(duration: Long) {
        this.duration = duration
    }

    fun durationToReadable(): String {
        val length = info?.length ?: 0L
        val hours = TimeUnit.MILLISECONDS.toHours(length)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(length) - TimeUnit.HOURS.toMinutes(hours)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(length) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length))

        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }

    fun durationToReadableFromDuration(): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(duration)
        )

        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }
}
