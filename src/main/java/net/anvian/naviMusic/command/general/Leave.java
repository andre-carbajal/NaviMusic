package net.anvian.naviMusic.command.general;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.command.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Leave implements ICommand {
    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leaves the voice channel";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, LavalinkClient client, Guild guild) {
        event.getJDA().getDirectAudioController().disconnect(guild);
        event.reply("Leaving your channel!").queue();
    }
}
