package net.anvian.naviMusic;

import net.anvian.naviMusic.commands.CommandManager;
import net.anvian.naviMusic.commands.music.Queue;
import net.anvian.naviMusic.commands.music.*;
import net.anvian.naviMusic.gui.ConsoleGUI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        String token = getToken(args);
        String guiOption = getGuiOption(args);

        checkGui(guiOption);

        JDA jda = JDABuilder.createDefault(token)
                .setActivity(Activity.customStatus("Waiting for the music"))
                .build();

        CommandManager manager = new CommandManager();
        manager.add(new NowPlaying());
        manager.add(new Play());
        manager.add(new Queue());
        manager.add(new Repeat());
        manager.add(new Skip());
        manager.add(new Stop());
        manager.add(new Shuffle());
        manager.add(new Volume());
        manager.add(new Pause());
        manager.add(new Continue());
        manager.add(new Remove());

        jda.addEventListener(manager);
    }

    private static void checkGui(String guiOption){
        if (guiOption.equals("nogui")) {
            System.setProperty("java.awt.headless", "true");
        }

        if (!GraphicsEnvironment.isHeadless()) {
            boolean isGuiVisible = !guiOption.equals("nogui");
            ConsoleGUI consoleGUI = new ConsoleGUI();
            consoleGUI.setVisible(isGuiVisible);
        }
    }

    private static String getToken(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isEmpty()) {
            if (args.length > 0) {
                token = args[0];
            } else {
                System.out.println("Please provide the Discord token as an environment variable or as a command line argument.");
                System.exit(1);
            }
        }

        return token;
    }

    private static String getGuiOption(String[] args) {
        if (System.getenv("DISCORD_TOKEN") != null && !System.getenv("DISCORD_TOKEN").isEmpty()) {
            return args.length > 0 ? args[0] : "";
        } else {
            return args.length > 1 ? args[1] : "";
        }
    }
}