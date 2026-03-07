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
        return formatTime(info?.length ?: 0L)
    }

    fun durationToReadableFromDuration(): String {
        return formatTime(duration)
    }

    companion object {
        @JvmStatic
        fun formatTime(millis: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(millis)
            )

            return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else String.format("%02d:%02d", minutes, seconds)
        }
    }
}
