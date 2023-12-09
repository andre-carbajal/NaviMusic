package net.anvian.naviMusic;

import io.github.cdimascio.dotenv.Dotenv;
import net.anvian.naviMusic.commands.CommandManager;
import net.anvian.naviMusic.commands.music.*;
import net.anvian.naviMusic.gui.ConsoleGUI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main {
    public static void main(String[] args) {
        ConsoleGUI consoleGUI = new ConsoleGUI();
        if (args.length == 0 || !args[0].equals("no-gui")) {
            consoleGUI.setVisible(true);
        }

        Dotenv dotenv = Dotenv.configure().load();

        JDA jda = JDABuilder.createDefault(dotenv.get("TOKEN"))
                .setActivity(Activity.customStatus("Waiting for the music"))
                .build();

        CommandManager manager = new CommandManager();
        manager.add(new NowPlaying());
        manager.add(new Play());
        manager.add(new Queue());
        manager.add(new Repeat());
        manager.add(new Skip());
        manager.add(new Stop());

        jda.addEventListener(manager);
    }
}