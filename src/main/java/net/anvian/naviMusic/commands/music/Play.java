package net.anvian.naviMusic.commands.music;

import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
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
        options.add(new OptionData(OptionType.STRING, "name-url-playlist", "Name of the song to play", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel").queue();
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if(!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            event.getGuild().getAudioManager().setSelfDeafened(true);
        } else {
            if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").queue();
                return;
            }
        }

        String name = event.getOption("name-url-playlist").getAsString();
        try {
            URI uri = new URI(name);
            if (uri.getHost() == null) {
                name = "ytsearch:" + name;
            }
        }catch (URISyntaxException e){
            name = "ytsearch:" + name;
        }

        PlayerManager playerManager = PlayerManager.get();

        if (name.contains("youtube.com/playlist?list=")) {
            playerManager.loadAndPlayPlaylist(event.getGuild(), name, event);
        } else {
            playerManager.play(event.getGuild(), name, event);
        }
    }
}