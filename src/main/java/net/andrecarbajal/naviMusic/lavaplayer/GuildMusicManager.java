package net.andrecarbajal.naviMusic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

public class GuildMusicManager {
    private final TrackScheduler trackScheduler;
    private final AudioForwarder audioForwarder;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        AudioPlayer player = manager.createPlayer();
        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
        audioForwarder = new AudioForwarder(player, guild);
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public AudioForwarder getAudioForwarder() {
        return audioForwarder;
    }

    public void queue(AudioTrack track) {
        trackScheduler.queue(track);
    }
}