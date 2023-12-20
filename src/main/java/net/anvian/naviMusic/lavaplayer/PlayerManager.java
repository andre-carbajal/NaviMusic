package net.anvian.naviMusic.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
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

    public void play(Guild guild, String trackURL, SlashCommandInteractionEvent event) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Play Command");
        embedBuilder.setThumbnail("https://i.imgur.com/xiiGqIO.png");
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.getTrackScheduler().queue(track);
                event.replyEmbeds(trackInformation(track, embedBuilder).build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                guildMusicManager.getTrackScheduler().queue(playlist.getTracks().get(0));
                event.replyEmbeds(trackInformation(playlist.getTracks().get(0), embedBuilder).build()).queue();
            }

            @Override
            public void noMatches() {
                event.replyEmbeds(embedBuilder.setDescription("No matches found for the song").build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.replyEmbeds(embedBuilder.setDescription("Could not play the song").build()).queue();
            }
        });
    }

    public void loadAndPlayPlaylist(final Guild guild, final String trackUrl, SlashCommandInteractionEvent event) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Play Command");
        embedBuilder.setThumbnail("https://i.imgur.com/xiiGqIO.png");
        audioPlayerManager.loadItemOrdered(guild, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println(track.getInfo().title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    musicManager.getTrackScheduler().queue(track);
                }
                event.replyEmbeds(playlistInformation(trackUrl, playlist.getTracks().size(), embedBuilder).build()).queue();
            }

            @Override
            public void noMatches() {
                event.replyEmbeds(embedBuilder.setDescription("No matches found for the playlist").build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.replyEmbeds(embedBuilder.setDescription("Could not play the playlist").build()).queue();
            }
        });
    }

    private EmbedBuilder trackInformation(AudioTrack track, EmbedBuilder embedBuilder) {
        AudioTrackInfo info = track.getInfo();
        embedBuilder.setDescription("Song added to queue:");
        embedBuilder.appendDescription("\n**Name:** `" + info.title + "`");
        embedBuilder.appendDescription("\n**Author:** `" + info.author + "`");
        embedBuilder.appendDescription("\n**URL:** `" + info.uri + "`");
        embedBuilder.appendDescription("\n**Duration:** `" + info.length / 60000 + ":" + info.length % 60000 / 1000 + "`");
        return embedBuilder;
    }

    private EmbedBuilder playlistInformation(String trackUrl, int songCount, EmbedBuilder embedBuilder) {
        embedBuilder.setDescription("\nPlaylist added to queue:");
        embedBuilder.appendDescription("\n**URL:** `" + trackUrl + "`");
        embedBuilder.appendDescription("\n**Songs added to queue:** `" + songCount + "`");
        return embedBuilder;
    }
}