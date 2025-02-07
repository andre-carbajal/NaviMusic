package net.andrecarbajal.naviMusic.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

    @Bean
    public JDA jda() {
        log.info("Starting Bot");

        if (token.equals("default"))
            throw new IllegalArgumentException("Bot token not specified in environment / application.properties");

        if (!listeners.contains(this)) listeners.add(this);

        JDA jda;
        try {

            jda = JDABuilder
                    .createDefault(token)
                    .setActivity(Activity.customStatus("Navi Music | /help"))
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build().awaitReady();

            listeners.forEach(jda::addEventListener);
        } catch (InterruptedException e) {
            log.error("Error starting JDA instance", e);
            return null;
        }

        Set<CommandData> commands = new HashSet<>();
        slashCommands.forEach(slashCommand -> {
            SlashCommandData data = Commands.slash(slashCommand.getName(), slashCommand.getDescription());

            if (slashCommand.getPermission() != null)
                data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(slashCommand.getPermission()));
            data.setGuildOnly(true);
            data.addOptions(slashCommand.getOptionDataList());
            commands.add(data);
        });
        jda.updateCommands().addCommands(commands).complete();

        log.info("Bot started");
        this.jda = jda;
        return jda;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down");

        try {
            this.jda.awaitShutdown();
        } catch (InterruptedException e) {
            log.error("Error during bot shutdown", e);
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
