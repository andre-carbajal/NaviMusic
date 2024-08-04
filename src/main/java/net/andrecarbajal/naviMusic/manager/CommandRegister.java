package net.andrecarbajal.naviMusic.manager;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandRegister {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final RestClient restClient;
    private static final String COMMAND_REGEX = "commands/*.json";

    public CommandRegister(RestClient restClient) {
        this.restClient = restClient;
    }

    public void registerCmds() throws IOException {
        final JacksonResources mapper = JacksonResources.create();
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();

        final ApplicationService appService = restClient.getApplicationService();
        final long appID = restClient.getApplicationId().block();
        final long guildID = Long.parseLong(
                System.getenv("TEST_SERVER") == null ? "9999" : System.getenv("TEST_SERVER"));
        List<ApplicationCommandRequest> commands = new ArrayList<>();

        for (Resource resource : matcher.getResources(COMMAND_REGEX)){
            ApplicationCommandRequest request = mapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);
            commands.add(request);
        }

        appService.bulkOverwriteGlobalApplicationCommand(appID, commands)
                .doOnNext(cmd -> LOGGER.debug("Successfully registered Global Command: {}", cmd.name()))
                .doOnError(err -> LOGGER.error("Failed to register global commands: {}", err.toString()))
                .subscribe();

        if (guildID != 9999)
            appService.bulkOverwriteGuildApplicationCommand(appID, guildID, commands)
                    .doOnNext(cmd -> LOGGER.debug("Successfully registered Guild command: {}", cmd.name()))
                    .doOnError(err -> LOGGER.error("Failed to register guild commands: {}", err.toString()))
                    .subscribe();
    }
}