package net.anvian.naviMusic.command.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.command.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Stop implements ICommand {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stops the current track";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, LavalinkClient client, Guild guild) {
        client.getOrCreateLink(guild.getIdLong())
                .updatePlayer(
                        (update) -> update.setTrack(null).setPaused(false)
                )
                .subscribe((__) -> {
                    event.reply("Stopped the current track").queue();
                });
    }
}
