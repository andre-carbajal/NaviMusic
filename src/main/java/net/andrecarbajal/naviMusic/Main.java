package net.andrecarbajal.naviMusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import net.andrecarbajal.naviMusic.manager.CommandManager;
import net.andrecarbajal.naviMusic.manager.CommandRegister;
import net.andrecarbajal.naviMusic.manager.GUIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("NaviMusic");
    private static final GUIManager guiManager = new GUIManager();

    public static final AudioPlayerManager PLAYER_MANAGER;

    static {
        PLAYER_MANAGER = new DefaultAudioPlayerManager();
        YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager();
        PLAYER_MANAGER.registerSourceManager(ytSourceManager);

        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
    }

    public static void main(String[] args) {
        guiManager.checkGui(getGuiOption(args));

        if (System.getenv("DISCORD_TOKEN") == null) {
            LOGGER.error("Discord Token is missing!");
            return;
        }
        if (System.getenv("SPOTIFY_CLIENT_ID") == null || System.getenv("SPOTIFY_SECRET") == null) {
            LOGGER.error("Spotify credentials are missing!");
            return;
        }

        final GatewayDiscordClient client = DiscordClientBuilder.create(System.getenv("DISCORD_TOKEN")).build()
                .gateway()
                .login().block();

        try {
            assert client != null;
            new CommandRegister(client.getRestClient()).registerCmds();
        } catch (Exception e) {
            LOGGER.error("Error trying to register commands: ", e);
        }

        Mono.when(client.on(ReadyEvent.class).doOnNext(readyEvent -> LOGGER.info("Logged in as {}",
                                readyEvent.getSelf().getUsername())),
                        client.on(ChatInputInteractionEvent.class, CommandManager::handle)
                                .doOnError(t -> LOGGER.error("Error with ChatInputInteractionEvent: {}", t.toString())),
                        client.onDisconnect().doOnTerminate(() -> LOGGER.info("Disconnected!")))
                .block();
    }

    private static String getGuiOption(String[] args) {
        if (System.getenv("DISCORD_TOKEN") != null && !System.getenv("DISCORD_TOKEN").isEmpty()) {
            return args.length > 0 ? args[0] : "";
        } else {
            return args.length > 1 ? args[1] : "";
        }
    }
}