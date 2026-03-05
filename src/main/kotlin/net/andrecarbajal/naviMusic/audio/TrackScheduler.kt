package net.andrecarbajal.naviMusic.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import org.slf4j.LoggerFactory
import java.util.concurrent.*

class TrackScheduler(private val player: AudioPlayer, private val guild: Guild) : AudioEventAdapter() {

    private val log = LoggerFactory.getLogger(TrackScheduler::class.java)

    val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()
    var isRepeating: Boolean = false

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var disconnectTask: ScheduledFuture<*>? = null

    fun queue(track: AudioTrack) {
        log.info("Adding {} in the queue", track.info.title)
        cancelDisconnectTimer()
        if (player.startTrack(track, true)) {
            logTrackStarted(track)
        } else {
            if (!queue.offer(track)) throw RuntimeException("Item was not added to the queue")
        }
    }

    fun nextTrack() {
        val track = queue.poll()
        if (track != null) {
            logTrackStarted(track)
        }
        player.startTrack(track, false)
    }

    fun skipTrack(position: Int) {
        if (position < 1) return

        if (position == 1) {
            nextTrack()
            return
        }

        val tracks = queue.toMutableList()
        if (position <= tracks.size) {
            tracks.removeAt(position - 1)
            queue.clear()
            queue.addAll(tracks)
        }
    }

    fun clear() {
        queue.clear()
        nextTrack()
        checkDisconnect()
    }

    fun getQueueSize(): Int = queue.size

    fun shuffle() {
        val tracks = queue.toMutableList()
        queue.clear()
        tracks.shuffle()
        tracks.forEach { queue(it) }
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            if (isRepeating) {
                player.startTrack(track.makeClone(), false)
                checkDisconnect()
                return
            }
            nextTrack()
            checkDisconnect()
            return
        }

        if (endReason == AudioTrackEndReason.LOAD_FAILED) {
            log.error("Error playing {} ({})", track.info.title, track.info.uri)
            nextTrack()
            checkDisconnect()
        }

        checkDisconnect()
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        cancelDisconnectTimer()
    }

    private fun checkDisconnect() {
        if (queue.isEmpty() && player.playingTrack == null) {
            scheduleDisconnect()
        }
    }

    private fun scheduleDisconnect() {
        disconnectTask?.cancel(false)
        disconnectTask = executor.schedule({
            guild.audioManager.closeAudioConnection()
        }, 3, TimeUnit.MINUTES)
    }

    private fun cancelDisconnectTimer() {
        disconnectTask?.cancel(false)
        disconnectTask = null
    }

    private fun logTrackStarted(track: AudioTrack) {
        val member = track.getUserData(Member::class.java)

        if (member != null) {
            log.info("Playing {} in {} requested by {}", track.info.title, member.guild.name, member.effectiveName)
        } else {
            log.info("Playing {}", track.info.title)
        }
    }
}
