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
        options.add(new OptionData(OptionType.STRING, "name", "Name of the song to play", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel").queue();
            System.out.println("User " + member.getEffectiveName() + " tried to use the play command but was not in a voice channel");
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
                System.out.println("User " + member.getEffectiveName() + " tried to use the play command but was not in the same voice channel as me");
                return;
            }
        }

        String name = event.getOption("name").getAsString();
        String songTitle = name;
        try {
            URI uri = new URI(name);
            if (uri.getHost().contains("youtube.com") && uri.getQuery().contains("list=")) {
                PlayerManager playerManager = PlayerManager.get();
                playerManager.loadAndPlayPlaylist(event.getGuild(), name);
                event.reply("Added songs from the playlist to the queue: " + songTitle).queue();
                System.out.println("User " + member.getEffectiveName() + " used the play command to queue a playlist");
            } else {
                playASong(event, songTitle, member, name);
            }
        } catch (URISyntaxException e) {
            name = "ytsearch:" + name;
            playASong(event, songTitle, member, name);
        }
    }
    private void playASong(SlashCommandInteractionEvent event , String songTitle, Member member , String name){
        PlayerManager playerManager = PlayerManager.get();
        event.reply("Playing: " + songTitle).queue();
        System.out.println("User " + member.getEffectiveName() + " used the play command");
        playerManager.play(event.getGuild(), name);
    }
}