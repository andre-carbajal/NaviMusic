package net.anvian.naviMusic.command.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import net.anvian.naviMusic.command.CommandUtils;
import net.anvian.naviMusic.command.ICommand;
import net.anvian.naviMusic.loader.AudioLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Play implements ICommand {
    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Will play a song";
    }

    @Override
    public List<OptionData> getOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "identifier", "The identifier of the song you want to play", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, LavalinkClient client, Guild guild) {
        if (guild.getSelfMember().getVoiceState().inAudioChannel()) {
            event.deferReply(false).queue();
        } else {
            CommandUtils.joinHelper(event);
        }

        final long guildId = guild.getIdLong();
        final Link link = client.getOrCreateLink(guildId);
        String identifier = event.getOption("identifier").getAsString();

        try {
            URI uri = new URI(identifier);
            if (uri.getHost() == null) {
                identifier = "ytsearch:" + identifier;
            }
        }catch (URISyntaxException e){
            identifier = "ytsearch:" + identifier;
        }

        link.loadItem(identifier).subscribe(new AudioLoader(link, event));
    }
}
