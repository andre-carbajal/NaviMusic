package net.anvian.naviMusic.listener;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.commands.CommandManager;
import net.anvian.naviMusic.commands.general.Help;

public class CommandInitializer {
    public CommandManager initialize(LavalinkClient client) {
        CommandManager manager = new CommandManager();
        manager.add(new Help(manager));

        return manager;
    }
}
