package net.anvian.naviMusic.command.general;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.command.CommandUtils;
import net.anvian.naviMusic.command.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Join implements ICommand {
    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Join the voice channel you are in.";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, LavalinkClient client, Guild guild) {
        CommandUtils.joinHelper(event);
    }
}
