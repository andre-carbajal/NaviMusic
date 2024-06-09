package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.commands.CommandUtils;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.lavaplayer.GuildMusicManager;
import net.andrecarbajal.naviMusic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Clear implements ICommand {
    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Clears the queue";
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
        guildMusicManager.getTrackScheduler().clearQueue();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Clear Command");
        embed.setThumbnail("https://i.imgur.com/xiiGqIO.png");
        event.replyEmbeds(embed.setDescription("The queue has been cleared.").build()).queue();
    }
}
