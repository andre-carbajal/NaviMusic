package net.anvian.naviMusic;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.anvian.naviMusic.client.LavalinkClientFactory;
import net.anvian.naviMusic.client.LavalinkNodeRegistrar;
import net.anvian.naviMusic.client.event.LavalinkEventListener;
import net.anvian.naviMusic.loader.CommandLoader;
import net.anvian.naviMusic.manager.TokenManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class NaviMusic {
    private static final TokenManager tokenManager = new TokenManager();

    private static final int SESSION_INVALID = 4006;

    public static void main(String[] args) throws InterruptedException {
        final var token = tokenManager.getToken(args);
        final LavalinkClient client = LavalinkClientFactory.createClient(token);
        final CommandLoader commandInitializer = new CommandLoader(client);

        LavalinkNodeRegistrar.registerNodes(client);
        LavalinkEventListener.registerListeners(client);

        final var jda = JDABuilder.createDefault(token)
                .setActivity(Activity.customStatus("Waiting for the music"))
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(client))
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(commandInitializer.initialize())
                .build()
                .awaitReady();

        client.on(WebSocketClosedEvent.class).subscribe((event) -> {
            if (event.getCode() == SESSION_INVALID) {
                final var guildId = event.getGuildId();
                final var guild = jda.getGuildById(guildId);

                if (guild == null) {
                    return;
                }

                final var connectedChannel = guild.getSelfMember().getVoiceState().getChannel();

                if (connectedChannel == null) {
                    return;
                }

                jda.getDirectAudioController().reconnect(connectedChannel);
            }
        });
    }
}