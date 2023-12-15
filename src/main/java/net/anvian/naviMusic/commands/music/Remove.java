package net.anvian.naviMusic.commands.music;

import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.GuildMusicManager;
import net.anvian.naviMusic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class Remove implements ICommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove a song from the queue";
    }

    @Override
    public List<OptionData> getOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.INTEGER, "number", "Removes the song from the queue in the given position", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int songNumber = (int) event.getOption("number").getAsLong();

        Guild guild = event.getGuild();
        GuildMusicManager musicManager = PlayerManager.get().getGuildMusicManager(guild);
        musicManager.getTrackScheduler().removeSongAt(songNumber - 1);

        event.reply("Song number " + songNumber + " has been removed from the queue.").queue();
    }
}