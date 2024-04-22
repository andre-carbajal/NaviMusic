package net.anvian.naviMusic.loader;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.command.CommandManager;
import net.anvian.naviMusic.command.general.Join;
import net.anvian.naviMusic.command.general.Leave;
import net.anvian.naviMusic.command.music.Nowplaying;
import net.anvian.naviMusic.command.music.Pause;
import net.anvian.naviMusic.command.music.Play;
import net.anvian.naviMusic.command.music.Stop;

public class CommandLoader {
    private final LavalinkClient client;

    public CommandLoader(LavalinkClient client) {
        this.client = client;
    }

    public CommandManager initialize() {
        CommandManager manager = new CommandManager(client);
        manager.add(new Join());
        manager.add(new Leave());

        manager.add(new Play());
        manager.add(new Stop());
        manager.add(new Nowplaying());
        manager.add(new Pause());

        return manager;
    }
}
