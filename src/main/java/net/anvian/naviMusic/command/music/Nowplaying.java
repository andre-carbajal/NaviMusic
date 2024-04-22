package net.anvian.naviMusic.command.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.anvian.naviMusic.MyUserData;
import net.anvian.naviMusic.command.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class Nowplaying implements ICommand {
    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getDescription() {
        return "Will display the current playing song";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, LavalinkClient client, Guild guild) {
        final var link = client.getOrCreateLink(guild.getIdLong());
        final var player = link.getCachedPlayer();

        if (player == null) {
            event.reply("Not connected or no player available!").queue();
            return;
        }

        final var track = player.getTrack();

        if (track == null) {
            event.reply("Nothing playing currently!").queue();
            return;
        }

        final var trackInfo = track.getInfo();

        event.reply(
                "Currently playing: %s\nDuration: %s/%s\nRequester: <@%s>".formatted(
                        trackInfo.getTitle(),
                        player.getPosition(),
                        trackInfo.getLength(),
                        track.getUserData(MyUserData.class).requester()
                )
        ).queue();
    }
}
