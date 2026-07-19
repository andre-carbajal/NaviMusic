package net.andrecarbajal.naviMusic.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class GuildMusicManager(
    manager: AudioPlayerManager,
    guild: Guild,
    private val maxPendingLoads: Int,
    onDisconnected: () -> Unit
) {
    private val stateExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "music-guild-${guild.idLong}").apply { isDaemon = true }
    }
    private val pendingLoads = ArrayDeque<((() -> Unit) -> Unit)>()
    private var loadInProgress = false
    private val loadCount = AtomicInteger(0)

    val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player, guild, { action -> submit(action) }, onDisconnected)

    init {
        player.addListener(scheduler)
    }

    fun getSendHandler(): AudioPlayerSendHandler {
        return AudioPlayerSendHandler(player)
    }

    fun <T> submit(action: () -> T): CompletableFuture<T> {
        val result = CompletableFuture<T>()
        stateExecutor.execute {
            try {
                result.complete(action())
            } catch (exception: Exception) {
                result.completeExceptionally(exception)
            }
        }
        return result
    }

    /** Starts only one provider load at a time, preserving slash-command arrival order. */
    fun enqueueLoad(load: ((() -> Unit) -> Unit)): Boolean {
        while (true) {
            val current = loadCount.get()
            if (current >= maxPendingLoads) return false
            if (loadCount.compareAndSet(current, current + 1)) break
        }
        submit {
            pendingLoads.addLast(load)
            startNextLoad()
        }
        return true
    }

    private fun startNextLoad() {
        if (loadInProgress) return
        val load = if (pendingLoads.isEmpty()) null else pendingLoads.removeFirst()
        if (load == null) return
        loadInProgress = true
        load {
            submit {
                loadInProgress = false
                loadCount.decrementAndGet()
                startNextLoad()
            }
        }
    }

    fun shutdown() {
        scheduler.shutdown()
        stateExecutor.shutdownNow()
    }
}
