package net.anvian.naviMusic.commands.music;

import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.AudioForwarder;
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
        if (forwarder != null) {
            if (forwarder.getAudioPlayer().isPaused()) {
                event.reply("The current song is already paused").queue();
                return;
            }
            forwarder.getAudioPlayer().setPaused(true);
            event.reply("Paused the current song").queue();
        } else {
            event.reply("No song is currently playing").queue();
        }
    }
}
