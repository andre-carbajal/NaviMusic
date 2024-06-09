package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.andrecarbajal.naviMusic.commands.CommandUtils;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.lavaplayer.AudioForwarder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;

public class Volume implements ICommand {
    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getDescription() {
        return "Change the volume of the music player.";
    }

    @Override
    public List<OptionData> getOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.INTEGER, "value", "Set the music player volume", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();

        if (!CommandUtils.validateVoiceState(event, member, self)) return;

        AudioManager audioManager = event.getGuild().getAudioManager();
        AudioPlayer player = ((AudioForwarder) audioManager.getSendingHandler()).getAudioPlayer();

        int volume = event.getOption("value").getAsInt();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Volume Command");
        embed.setThumbnail("https://i.imgur.com/xiiGqIO.png");

        if ((volume < 0 || volume > 100) && volume != 500) {
            event.replyEmbeds(embed.setDescription("Volume must be between 0 and 100").build()).queue();
            return;
        }

        player.setVolume(volume);
        event.replyEmbeds(embed.setDescription("Volume set to `" + volume + "`").build()).queue();
    }
}
