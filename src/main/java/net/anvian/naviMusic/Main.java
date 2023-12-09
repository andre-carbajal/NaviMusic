package net.anvian.naviMusic;

import io.github.cdimascio.dotenv.Dotenv;
import net.anvian.naviMusic.commands.CommandManager;
import net.anvian.naviMusic.commands.music.*;
import net.anvian.naviMusic.gui.ConsoleGUI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("nogui")) {
            System.setProperty("java.awt.headless", "true");
        }

        if (!GraphicsEnvironment.isHeadless()) {
            boolean isGuiVisible = args.length == 0 || !args[0].equals("nogui");
            ConsoleGUI consoleGUI = new ConsoleGUI();
            consoleGUI.setVisible(isGuiVisible);
            System.out.println("Running in headless mode");
        }else
            System.out.println("Not running in headless mode");

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