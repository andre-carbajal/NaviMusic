package net.anvian.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.anvian.naviMusic.commands.CommandUtils;
import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.GuildMusicManager;
import net.anvian.naviMusic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NowPlaying implements ICommand {
    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getDescription() {
        return "Will display the current playing song";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();

        if (!CommandUtils.validateVoiceState(event, member, self)) return;

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        if(guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack() == null) {
            event.reply("I am not playing anything").queue();
            return;
        }
        EmbedBuilder embedBuilder = getBuilder(guildMusicManager);
        event.replyEmbeds(embedBuilder.build()).queue();
    }

    @NotNull
    private static EmbedBuilder getBuilder(GuildMusicManager guildMusicManager) {
        AudioTrackInfo info = guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack().getInfo();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Currently Playing");
        embedBuilder.setThumbnail("https://i.imgur.com/xiiGqIO.png");
        embedBuilder.setDescription("**Name:** `" + info.title + "`");
        embedBuilder.appendDescription("\n**Author:** `" + info.author + "`");
        embedBuilder.appendDescription("\n**URL:** `" + info.uri + "`");
        embedBuilder.appendDescription("\n**Duration:** `" + info.length / 60000 + ":" + String.format("%02d", info.length % 60000 / 1000) + "`");
        return embedBuilder;
    }
}