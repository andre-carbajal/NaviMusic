package net.anvian.naviMusic.commands.music;

import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.AudioForwarder;
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
        if (forwarder != null) {
            if (!forwarder.getAudioPlayer().isPaused()) {
                event.reply("The current song is already playing").queue();
                return;
            }
            forwarder.getAudioPlayer().setPaused(false);
            event.reply("Continued the current song").queue();
        } else {
            event.reply("No song is currently playing").queue();
        }
    }
}
