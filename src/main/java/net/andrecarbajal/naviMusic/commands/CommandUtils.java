package net.andrecarbajal.naviMusic.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandUtils {

    public static boolean validateVoiceState(SlashCommandInteractionEvent event, Member member, Member self) {
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if(!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel").queue();
            return false;
        }

        if(!selfVoiceState.inAudioChannel()) {
            event.reply("I am not in an audio channel").queue();
            return false;
        }

        if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You are not in the same channel as me").queue();
            return false;
        }

        return true;
    }
}