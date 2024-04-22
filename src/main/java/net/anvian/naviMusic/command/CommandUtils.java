package net.anvian.naviMusic.command;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandUtils {
    SlashCommandInteractionEvent event;
    public CommandUtils(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    public static void joinHelper(SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState.inAudioChannel()) {
            event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
        }

        event.reply("Joining your channel!").queue();
    }
}
