package net.anvian.naviMusic.command.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.command.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Pause implements ICommand {
    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Pause or unpause the player";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, LavalinkClient client, Guild guild) {
        client.getOrCreateLink(guild.getIdLong())
                .getPlayer()
                .flatMap((player) -> player.setPaused(!player.getPaused()))
                .subscribe((player) -> {
                    event.reply("Player has been " + (player.getPaused() ? "paused" : "resumed") + "!").queue();
                });
    }
}
