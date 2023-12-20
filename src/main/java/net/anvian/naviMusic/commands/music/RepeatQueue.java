package net.anvian.naviMusic.commands.music;

import net.anvian.naviMusic.commands.CommandUtils;
import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.GuildMusicManager;
import net.anvian.naviMusic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class RepeatQueue implements ICommand {
    @Override
    public String getName() {
        return "repeatqueue";
    }

    @Override
    public String getDescription() {
        return "Repeats the queue";
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
        boolean isRepeatQueue = !guildMusicManager.getTrackScheduler().isRepeatQueue();
        guildMusicManager.getTrackScheduler().setRepeatQueue(isRepeatQueue);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Repeat Queue Command");
        embed.setThumbnail("https://i.imgur.com/xiiGqIO.png");

        event.replyEmbeds(embed.setDescription("Repeat queue is now " + isRepeatQueue).build()).queue();
    }
}
