package net.anvian.naviMusic.command.general;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.command.CommandManager;
import net.anvian.naviMusic.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Help implements ICommand {
    private final CommandManager commandManager;

    public Help(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows the list of commands and their descriptions.";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, LavalinkClient client, Guild guild) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("List of Commands");
        embedBuilder.setThumbnail("https://i.imgur.com/xiiGqIO.png");

        for (ICommand command : commandManager.getCommands()) {
            StringBuilder commandInfo = new StringBuilder("/" + command.getName());
            List<OptionData> options = command.getOptions();
            if (options != null) {
                for (OptionData option : options) {
                    commandInfo.append(" <").append(option.getName()).append(">");
                }
            }
            embedBuilder.addField(commandInfo.toString(), command.getDescription(), false);
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
