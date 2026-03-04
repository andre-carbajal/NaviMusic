package net.andrecarbajal.naviMusic.config;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BotConfiguration extends ListenerAdapter {
    private final List<SlashCommand> slashCommands;

    private final List<ListenerAdapter> listeners;

    @Value("${app.discord.token}")
    private String token;

    private JDA jda;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent() {
        if (jda != null) return;

        log.info("Starting Bot");

        if (token == null || token.equals("default") || token.isEmpty()) {
            log.error("Bot token not specified in environment / application.properties");
            return;
        }

        try {
            jda = JDABuilder
                    .createDefault(token)
                    .setActivity(Activity.customStatus("Navi Music | /help"))
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(new JDaveSessionFactory()))
                    .build().awaitReady();

            jda.addEventListener(this);
            listeners.forEach(listener -> {
                if (listener != this) {
                    jda.addEventListener(listener);
                }
            });

            registerCommands();
            log.info("Bot started and commands registered");
        } catch (InterruptedException e) {
            log.error("Error starting JDA instance", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error during JDA initialization", e);
        }
    }

    private void registerCommands() {
        Set<CommandData> commands = new HashSet<>();
        slashCommands.forEach(slashCommand -> {
            SlashCommandData data = Commands.slash(slashCommand.getName(), slashCommand.getDescription());

            if (slashCommand.getPermission() != null)
                data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(slashCommand.getPermission()));
            data.setContexts(InteractionContextType.GUILD);
            data.addOptions(slashCommand.getOptionDataList());
            commands.add(data);
        });
        jda.updateCommands().addCommands(commands).queue();
    }

    @Bean
    public JDA jdaInstance() {
        return jda;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down");
        if (jda != null) {
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Error during bot shutdown", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        Optional<SlashCommand> commandOptional = slashCommands.parallelStream().filter(
                slashCommand -> slashCommand.getName().equalsIgnoreCase(e.getName())
        ).findFirst();

        commandOptional.ifPresent(slashCommand -> slashCommand.onCommand(e));
    }
}
