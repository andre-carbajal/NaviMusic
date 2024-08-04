package net.andrecarbajal.naviMusic.manager;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.commands.general.Help;
import net.andrecarbajal.naviMusic.commands.music.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final static List<ICommand> commands = new ArrayList<>();

    static {
        // General Commands
        commands.add(new Help());

        // Music Commands
        commands.add(new Join());
        commands.add(new Leave());
        commands.add(new Play());
        commands.add(new PlayFromFile());
        commands.add(new Stop());
        commands.add(new Skip());
        commands.add(new Pause());
        commands.add(new Continue());
        commands.add(new Queue());
        commands.add(new Shuffle());
        commands.add(new NowPlaying());
        commands.add(new Repeat());
        commands.add(new RepeatOff());
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    public static Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .flatMap(command -> command.handle(event));
    }
}
