package net.andrecarbajal.naviMusic.lavaplayer.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.andrecarbajal.naviMusic.lavaplayer.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TrackLoadResultHandler implements AudioLoadResultHandler {
    private final GuildMusicManager guildMusicManager;
    private final SlashCommandInteractionEvent event;
    private final EmbedBuilder embedBuilder = new EmbedBuilder().setThumbnail("https://i.imgur.com/xiiGqIO.png");

    public TrackLoadResultHandler(GuildMusicManager guildMusicManager, SlashCommandInteractionEvent event) {
        this.guildMusicManager = guildMusicManager;
        this.event = event;
    }

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

    private EmbedBuilder trackInformation(AudioTrack track, EmbedBuilder embedBuilder) {
        AudioTrackInfo info = track.getInfo();
        embedBuilder.setDescription("Song added to queue:");
        embedBuilder.appendDescription("\n**Name:** `" + info.title + "`");
        embedBuilder.appendDescription("\n**Author:** `" + info.author + "`");
        embedBuilder.appendDescription("\n**URL:** `" + info.uri + "`");
        embedBuilder.appendDescription("\n**Duration:** `" + info.length / 60000 + ":" + String.format("%02d", info.length % 60000 / 1000) + "`");
        return embedBuilder;
    }
}
