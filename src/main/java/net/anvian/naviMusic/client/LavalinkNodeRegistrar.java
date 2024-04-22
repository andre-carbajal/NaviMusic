package net.anvian.naviMusic.client;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LavalinkNodeRegistrar {
    private static final Logger LOG = LoggerFactory.getLogger(LavalinkNodeRegistrar.class);
    public static void registerNodes(LavalinkClient client) {
        List.of(
                client.addNode(
                        new NodeOptions.Builder()
                                .setName("DevelopNode")
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
}