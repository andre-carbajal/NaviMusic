package net.anvian.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.AudioForwarder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
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
        event.deferReply().queue();
        Member member = event.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel").queue();
            System.out.println("User " + member.getEffectiveName() + " tried to use the queue command but was not in a voice channel");
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if(!selfVoiceState.inAudioChannel()) {
            event.reply("I am not in an audio channel").queue();
            System.out.println("User " + member.getEffectiveName() + " tried to use the queue command but I am not in an audio channel");
            return;
        }

        if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You are not in the same channel as me").queue();
            System.out.println("User " + member.getEffectiveName() + " tried to use the queue command but was not in the same voice channel as me");
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        AudioPlayer player = ((AudioForwarder) audioManager.getSendingHandler()).getAudioPlayer();

        int volume = event.getOption("value").getAsInt();

        if ((volume < 0 || volume > 100) && volume != 500) {
            event.getHook().editOriginal("Volume must be between 0 and 100").queue();
            return;
        }

        player.setVolume(volume);
        event.getHook().editOriginal("Volume set to " + volume).queue();
    }
}
