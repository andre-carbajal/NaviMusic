package net.anvian.naviMusic;

import net.anvian.naviMusic.commands.CommandManager;
import net.anvian.naviMusic.listener.CommandInitializer;
import net.anvian.naviMusic.manager.GUIManager;
import net.anvian.naviMusic.manager.TokenManager;
import net.anvian.naviMusic.manager.VoiceChannelManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main {
    private static final TokenManager tokenManager = new TokenManager();
    private static final GUIManager guiManager = new GUIManager();
    private static final VoiceChannelManager voiceChannelManager = new VoiceChannelManager();
    private static final CommandInitializer commandInitializer = new CommandInitializer();

    public static void main(String[] args) {
        guiManager.checkGui(getGuiOption(args));

        String token = tokenManager.getToken(args);
        JDA jda = JDABuilder.createDefault(token)
                .setActivity(Activity.customStatus("Waiting for the music"))
                .build();


        CommandManager manager = commandInitializer.initialize();
        jda.addEventListener(manager);

        voiceChannelManager.scheduleDisconnect(jda);
    }

    private static String getGuiOption(String[] args) {
        if (System.getenv("DISCORD_TOKEN") != null && !System.getenv("DISCORD_TOKEN").isEmpty()) {
            return args.length > 0 ? args[0] : "";
        } else {
            return args.length > 1 ? args[1] : "";
        }
    }
}