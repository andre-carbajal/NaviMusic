package net.anvian.naviMusic.commands.music;

import net.anvian.naviMusic.commands.CommandUtils;
import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.GuildMusicManager;
import net.anvian.naviMusic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class Remove implements ICommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove a song from the queue";
    }

    @Override
    public List<OptionData> getOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.INTEGER, "number", "Removes the song from the queue in the given position", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();

        if (!CommandUtils.validateVoiceState(event, member, self)) return;

        int songNumber = (int) event.getOption("number").getAsLong();

        Guild guild = event.getGuild();
        GuildMusicManager musicManager = PlayerManager.get().getGuildMusicManager(guild);
        musicManager.getTrackScheduler().removeSongAt(songNumber - 1);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Remove Command");
        embed.setThumbnail("https://i.imgur.com/xiiGqIO.png");

        event.replyEmbeds(embed.setDescription("Song number " + songNumber + " has been removed from the queue.").build()).queue();
    }
}
