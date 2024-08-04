package net.andrecarbajal.naviMusic.commands.general;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.manager.CommandManager;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;

public class Help implements ICommand {
    private final CommandManager commandManager = new CommandManager();

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getDescription() {
        return "Shows the list of commands and their descriptions.";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        EmbedCreateSpec.Builder embedBuilder = EmbedCreator.createEmbed("List of commands");

        for (ICommand command : commandManager.getCommands()) {
            embedBuilder.addField("/" + command.getName(), command.getDescription(), false);
        }
        return event.reply().withEmbeds(embedBuilder.build()).then();
    }
}
