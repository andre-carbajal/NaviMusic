package net.andrecarbajal.naviMusic.util;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.andrecarbajal.naviMusic.audio.spotify.SpotifyPlaylist;
import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedCreator {
    public static EmbedBuilder createEmbed(String title) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setThumbnail("https://i.imgur.com/xiiGqIO.png");
        return embedBuilder;
    }

    public static EmbedBuilder createEmbed(String title, String description) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(description);
        embedBuilder.setThumbnail("https://i.imgur.com/xiiGqIO.png");
        return embedBuilder;
    }

    public static EmbedBuilder createEmbedPlaylist(EmbedBuilder embed, String trackUrl, SpotifyPlaylist playlist) {
        embed.addField("", "\nPlaylist added to queue:", false);
        embed.addField("", "\n**URL:** `" + trackUrl + "`", false);
        embed.addField("", "\n**Songs added to queue:** `" + playlist.getSongs().length + "`", false);
        return embed;
    }

    public static EmbedBuilder createEmbedPlaylist(EmbedBuilder embed, String trackUrl, AudioPlaylist playlist) {
        embed.addField("", "\nPlaylist added to queue:", false);
        embed.addField("", "\n**URL:** `" + trackUrl + "`", false);
        embed.addField("", "\n**Songs added to queue:** `" + playlist.getTracks().size() + "`", false);
        return embed;
    }

    public static EmbedBuilder createEmbedSongs(EmbedBuilder embed, AudioTrackInfo info) {
        embed.addField("", "\nSong added to queue:", false);
        embed.addField("", "\n**Title:** `" + info.title + "`", false);
        embed.addField("", "\n**Author:** `" + info.author + "`", false);
        embed.addField("", "\n**URL:** `" + (info.uri != null ? info.uri : "N/A") + "`", false);
        embed.addField("", "\n**Duration:** `" + formatDuration(info.length) + "`", false);
        return embed;
    }

    public static EmbedBuilder createEmbedSongs(EmbedBuilder embed, AudioTrackInfo info, String name) {
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
