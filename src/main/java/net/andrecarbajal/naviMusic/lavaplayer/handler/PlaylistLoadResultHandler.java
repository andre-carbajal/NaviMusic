package net.andrecarbajal.naviMusic.lavaplayer.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.andrecarbajal.naviMusic.lavaplayer.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PlaylistLoadResultHandler implements AudioLoadResultHandler {
    private final GuildMusicManager musicManager;
    private final SlashCommandInteractionEvent event;
    private final String trackUrl;
    private final EmbedBuilder embedBuilder = new EmbedBuilder().setThumbnail("https://i.imgur.com/xiiGqIO.png");

    public PlaylistLoadResultHandler(GuildMusicManager musicManager, SlashCommandInteractionEvent event, String trackUrl) {
        this.musicManager = musicManager;
        this.event = event;
        this.trackUrl = trackUrl;
    }

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

    private EmbedBuilder playlistInformation(String trackUrl, int songCount, EmbedBuilder embedBuilder) {
        embedBuilder.setDescription("\nPlaylist added to queue:");
        embedBuilder.appendDescription("\n**URL:** `" + trackUrl + "`");
        embedBuilder.appendDescription("\n**Songs added to queue:** `" + songCount + "`");
        return embedBuilder;
    }
}
