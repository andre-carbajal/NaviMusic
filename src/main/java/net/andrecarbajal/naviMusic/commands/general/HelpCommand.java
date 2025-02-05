package net.andrecarbajal.naviMusic.commands.general;

import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HelpCommand extends SlashCommand {
    private final List<SlashCommand> slashCommands;

    public HelpCommand(List<SlashCommand> slashCommands) {
        super("help", "Shows the list of commands and their descriptions", Category.GENERAL);
        this.slashCommands = slashCommands;
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("**General Commands:**\n");
        slashCommands.stream().filter(slashCommand -> slashCommand.getCategory() == Category.GENERAL).forEach(slashCommand -> {
            sb.append("`").append("/").append(slashCommand.getName()).append("`").append(" - ").append(slashCommand.getDescription()).append("\n");
        });
        sb.append("\n");
        sb.append("**Music Commands:**\n");
        slashCommands.stream().filter(slashCommand -> slashCommand.getCategory() == Category.MUSIC).forEach(slashCommand -> {
            sb.append("`").append("/").append(slashCommand.getName()).append("`").append(" - ").append(slashCommand.getDescription()).append("\n");
        });
        new Response(sb.toString(), Response.Type.OK, false).sendReply(event);
    }
}
