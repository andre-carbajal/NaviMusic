package net.anvian.naviMusic;

import dev.arbjerg.lavalink.client.*;
import net.anvian.naviMusic.commands.CommandManager;
import net.anvian.naviMusic.listener.CommandInitializer;
import net.anvian.naviMusic.manager.GUIManager;
import net.anvian.naviMusic.manager.TokenManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final TokenManager tokenManager = new TokenManager();
    private static final GUIManager guiManager = new GUIManager();
    private static final CommandInitializer commandInitializer = new CommandInitializer();

    public static void main(String[] args) {
        guiManager.checkGui(getGuiOption(args));

        String token = tokenManager.getToken(args);

        final LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(token));

        registerLavalinkListeners(client);
        registerLavalinkNodes(client);

        JDA jda = JDABuilder.createDefault(token)
                .setActivity(Activity.customStatus("Waiting for the music"))
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .build();


        CommandManager manager = commandInitializer.initialize(client);
        jda.addEventListener(manager);
    }

    private static void registerLavalinkListeners(LavalinkClient client) {
        client.on(ReadyEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();

            LOG.info(
                    "Node '{}' is ready, session id is '{}'!",
                    node.getName(),
                    event.getSessionId()
            );
        });

        client.on(StatsEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();

            LOG.info(
                    "Node '{}' has stats, current players: {}/{} (link count {})",
                    node.getName(),
                    event.getPlayingPlayers(),
                    event.getPlayers(),
                    client.getLinks().size()
            );
        });

        client.on(EmittedEvent.class).subscribe((event) -> {
            if (event instanceof TrackStartEvent) {
                LOG.info("Is a track start event!");
            }

            final var node = event.getNode();

            LOG.info(
                    "Node '{}' emitted event: {}",
                    node.getName(),
                    event
            );
        });
    }

    private static void registerLavalinkNodes(LavalinkClient client) {
        List.of(
                client.addNode(
                        new NodeOptions.Builder()
                                .setName("Node")
                                .setServerUri("ws://localhost:2333")
                                .setPassword("youshallnotpass")
                                .build()
                )
        ).forEach((node) -> {
            node.on(TrackStartEvent.class).subscribe((event) -> {
                final LavalinkNode node1 = event.getNode();

                LOG.info(
                        "{}: track started: {}",
                        node1.getName(),
                        event.getTrack().getInfo()
                );
            });
        });
    }

    private static String getGuiOption(String[] args) {
        if (System.getenv("DISCORD_TOKEN") != null && !System.getenv("DISCORD_TOKEN").isEmpty()) {
            return args.length > 0 ? args[0] : "";
        } else {
            return args.length > 1 ? args[1] : "";
        }
    }
}