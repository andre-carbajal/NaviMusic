package net.andrecarbajal.naviMusic.commands.music;

import net.andrecarbajal.naviMusic.commands.CommandUtils;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.lavaplayer.AudioForwarder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class Continue implements ICommand {
    @Override
    public String getName() {
        return "continue";
    }

    @Override
    public String getDescription() {
        return "Continue the song if it is paused";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        AudioForwarder forwarder = (AudioForwarder) audioManager.getSendingHandler();

        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();

        if (!CommandUtils.validateVoiceState(event, member, self)) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Continue Command");
        embed.setThumbnail("https://i.imgur.com/xiiGqIO.png");

        if (forwarder != null) {
            if (!forwarder.getAudioPlayer().isPaused()) {
                event.replyEmbeds(embed.setDescription("The current song is already playing").build()).queue();
                return;
            }
            forwarder.getAudioPlayer().setPaused(false);
            event.replyEmbeds(embed.setDescription("Continued the current song").build()).queue();
        } else {
            event.replyEmbeds(embed.setDescription("No song is currently playing").build()).queue();
        }
    }
}
