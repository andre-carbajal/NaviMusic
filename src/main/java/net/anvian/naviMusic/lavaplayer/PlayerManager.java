package net.anvian.naviMusic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    private PlayerManager() {
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager get() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager, guild);

            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());

            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL, SlashCommandInteractionEvent event, String songTitle) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.getTrackScheduler().queue(track);
                event.reply("Playing: " + songTitle).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                guildMusicManager.getTrackScheduler().queue(playlist.getTracks().get(0));
                event.reply("Playing: " + songTitle).queue();
            }

            @Override
            public void noMatches() {
               event.reply("No matches found for " + songTitle).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.reply("Could not play: " + songTitle).queue();
            }
        });
    }

    public void loadAndPlayPlaylist(final Guild guild, final String trackUrl, SlashCommandInteractionEvent event, String songTitle) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);
        audioPlayerManager.loadItemOrdered(guild, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    musicManager.getTrackScheduler().queue(track);
                }
                event.reply("Playing: " + songTitle).queue();
            }

            @Override
            public void noMatches() {
                event.reply("No matches found for " + songTitle).queue();
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                event.reply("Could not play: " + songTitle).queue();
            }
        });
    }
}