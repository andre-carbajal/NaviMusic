package net.andrecarbajal.naviMusic.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface ICommand {
    default String getName() {
        return "COMMAND_NAME";
    }

    default String getCategory() {
        return "COMMAND_CATEGORY";
    }

    default String getDescription() {
        return "COMMAND_DESCRIPTION";
    }

    default Mono<Void> handle(ChatInputInteractionEvent event) {
        return null;
    }
}