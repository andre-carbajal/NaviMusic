package net.anvian.naviMusic.client;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;

public class LavalinkClientFactory {
    public static LavalinkClient createClient(String token) {
        final LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(token));
        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        return client;
    }
}