package net.anvian.naviMusic.listener;

import net.anvian.naviMusic.commands.CommandManager;
import net.anvian.naviMusic.commands.general.Help;
import net.anvian.naviMusic.commands.music.*;

public class CommandInitializer {
    public CommandManager initialize() {
        CommandManager manager = new CommandManager();
        manager.add(new Help(manager));
        manager.add(new Play());
        manager.add(new NowPlaying());
        manager.add(new Remove());
        manager.add(new Repeat());
        manager.add(new Queue());
        manager.add(new Skip());
        manager.add(new Stop());
        manager.add(new Shuffle());
        manager.add(new Volume());
        manager.add(new Pause());
        manager.add(new Continue());
        return manager;
    }
}