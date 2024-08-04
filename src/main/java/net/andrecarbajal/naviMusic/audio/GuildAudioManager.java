package net.andrecarbajal.naviMusic.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.andrecarbajal.naviMusic.Main.PLAYER_MANAGER;

public class GuildAudioManager {

    private static final Map<Snowflake, GuildAudioManager> MANAGERS = new ConcurrentHashMap<>();

    public static GuildAudioManager of(Snowflake id) {
        return MANAGERS.computeIfAbsent(id, ignored -> new GuildAudioManager());
    }

    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final LavaPlayerAudioProvider provider;

    private GuildAudioManager() {
        player = PLAYER_MANAGER.createPlayer();
        scheduler = new TrackScheduler(player);
        provider = new LavaPlayerAudioProvider(player);

        player.addListener(scheduler);
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public LavaPlayerAudioProvider getProvider() {
        return provider;
    }
}
