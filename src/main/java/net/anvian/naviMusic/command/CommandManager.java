package net.anvian.naviMusic.command;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CommandManager.class);
    private final LavalinkClient client;
    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager(LavalinkClient client) {
        this.client = client;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOG.info("{} is ready!", event.getJDA().getSelfUser().getAsTag());

        for (Guild guild : event.getJDA().getGuilds()) {
            for (ICommand command : commands) {
                if (command.getOptions() == null) {
                    guild.upsertCommand(command.getName(), command.getDescription()).queue();
                } else {
                    guild.upsertCommand(command.getName(), command.getDescription()).addOptions(command.getOptions()).queue();
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        for (ICommand command : commands) {
            if (command.getName().equals(event.getName())) {
                command.execute(event, this.client, guild);
                return;
            }
        }
    }

    public void add(ICommand command) {
        commands.add(command);
    }
}
