package net.andrecarbajal.naviMusic.util;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.spec.EmbedCreateSpec;
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyPlaylist;

public class EmbedCreator {
    public static EmbedCreateSpec.Builder createEmbed(String title) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();
        embedBuilder.title(title);
        embedBuilder.thumbnail("https://i.imgur.com/xiiGqIO.png");
        return embedBuilder;
    }

    public static EmbedCreateSpec.Builder createEmbed(String title, String description) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();
        embedBuilder.title(title);
        embedBuilder.description(description);
        embedBuilder.thumbnail("https://i.imgur.com/xiiGqIO.png");
        return embedBuilder;
    }

    public static EmbedCreateSpec.Builder createEmbedPlaylist(EmbedCreateSpec.Builder embed, String trackUrl, SpotifyPlaylist playlist) {
        embed.addField("", "\nPlaylist added to queue:", false);
        embed.addField("", "\n**URL:** `" + trackUrl + "`", false);
        embed.addField("", "\n**Songs added to queue:** `" + playlist.getSongs().length + "`", false);
        return embed;
    }

    public static EmbedCreateSpec.Builder createEmbedPlaylist(EmbedCreateSpec.Builder embed, String trackUrl, AudioPlaylist playlist) {
        embed.addField("", "\nPlaylist added to queue:", false);
        embed.addField("", "\n**URL:** `" + trackUrl + "`", false);
        embed.addField("", "\n**Songs added to queue:** `" + playlist.getTracks().size() + "`", false);
        return embed;
    }

    public static EmbedCreateSpec.Builder createEmbedSongs(EmbedCreateSpec.Builder embed, AudioTrackInfo info) {
        embed.addField("", "\nSong added to queue:", false);
        embed.addField("", "\n**Title:** `" + info.title + "`", false);
        embed.addField("", "\n**Author:** `" + info.author + "`", false);
        embed.addField("", "\n**URL:** `" + (info.uri != null ? info.uri : "N/A") + "`", false);
        embed.addField("", "\n**Duration:** `" + formatDuration(info.length) + "`", false);
        return embed;
    }

    public static EmbedCreateSpec.Builder createEmbedSongs(EmbedCreateSpec.Builder embed, AudioTrackInfo info, String name) {
        embed.addField("", "\nSong added to queue:", false);
        embed.addField("", "\n**Title:** `" + name + "`", false);
        embed.addField("", "\n**URL:** `" + (info.uri != null ? info.uri : "N/A") + "`", false);
        embed.addField("", "\n**Duration:** `" + formatDuration(info.length) + "`", false);
        return embed;
    }

    private static String formatDuration(long durationMillis) {
        long hours = durationMillis / 3600000;
        long minutes = durationMillis / 60000;
        long seconds = (durationMillis % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
