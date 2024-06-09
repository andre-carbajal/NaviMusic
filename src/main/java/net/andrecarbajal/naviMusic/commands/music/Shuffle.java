package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.andrecarbajal.naviMusic.commands.CommandUtils;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.lavaplayer.GuildMusicManager;
import net.andrecarbajal.naviMusic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shuffle implements ICommand {
    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getDescription() {
        return "Shuffles the queue";
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
        List<AudioTrack> queue = new ArrayList<>(guildMusicManager.getTrackScheduler().getQueue());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Shuffle Command");
        embed.setThumbnail("https://i.imgur.com/xiiGqIO.png");

        if(queue.isEmpty()) {
            event.replyEmbeds(embed.setDescription("An empty list cannot be shuffled").build()).queue();
            return;
        }

        Collections.shuffle(queue);
        guildMusicManager.getTrackScheduler().setQueue(queue);
        event.replyEmbeds(embed.setDescription("The queue has been shuffled").build()).queue();
    }
}
