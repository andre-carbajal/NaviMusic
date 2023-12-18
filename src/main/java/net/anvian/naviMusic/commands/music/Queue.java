package net.anvian.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.anvian.naviMusic.commands.ICommand;
import net.anvian.naviMusic.lavaplayer.GuildMusicManager;
import net.anvian.naviMusic.lavaplayer.PlayerManager;
import net.anvian.naviMusic.listener.ButtonClickEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class Queue implements ICommand {
    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getDescription() {
        return "Will display the current queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
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
            event.reply("I am not in an audio channel").queue();
            return;
        }

        if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You are not in the same channel as me").queue();
            return;
        }

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(guildMusicManager.getTrackScheduler().getQueue());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Current Queue");

        int startIndex = 0;
        int endIndex = Math.min(startIndex + 10, queue.size());

        StringBuilder sb = new StringBuilder();

        if(!queue.isEmpty()) {
            for(int i = startIndex; i < endIndex; i++) {
                AudioTrackInfo info = queue.get(i).getInfo();
                sb.append(i+1).append(" : ").append(info.title).append("\n");
            }
            embedBuilder.addField("Queue:", sb.toString(), false);
        } else {
            embedBuilder.setDescription("Queue is empty");
        }

        InteractionHook hook = event.deferReply().complete();
        Message originalMessage;

        if(queue.size() < 10) {
            hook.sendMessageEmbeds(embedBuilder.build()).complete();
        } else {
            originalMessage = hook.sendMessageEmbeds(embedBuilder.build()).addActionRow(BUTTONS).complete();
            event.getJDA().addEventListener(new ButtonClickEventListener(queue, originalMessage));
        }
    }

    private static final List<Button> BUTTONS = List.of(
            Button.primary("page_first", Emoji.fromUnicode("⏪")),
            Button.primary("page_previous", Emoji.fromUnicode("◀")),
            Button.danger("page_cancel", Emoji.fromUnicode("❌")),
            Button.primary("page_next", Emoji.fromUnicode("▶")),
            Button.primary("page_last", Emoji.fromUnicode("⏩"))
    );
}