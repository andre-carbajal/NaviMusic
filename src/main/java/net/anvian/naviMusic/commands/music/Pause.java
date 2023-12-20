package net.anvian.naviMusic.commands.music;

import net.anvian.naviMusic.commands.CommandUtils;
import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.AudioForwarder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class Pause implements ICommand {
    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Pause the song if the song is playing";
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
        embed.setTitle("Pause Command");
        embed.setThumbnail("https://i.imgur.com/xiiGqIO.png");

        if (forwarder != null) {
            if (forwarder.getAudioPlayer().isPaused()) {
                event.replyEmbeds(embed.setDescription("The current song is already playing").build()).queue();
                return;
            }
            forwarder.getAudioPlayer().setPaused(true);
            event.replyEmbeds(embed.setDescription("Paused the current song").build()).queue();
        } else {
            event.replyEmbeds(embed.setDescription("No song is currently playing").build()).queue();
        }
    }
}
