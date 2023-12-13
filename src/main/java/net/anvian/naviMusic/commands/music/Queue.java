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
            System.out.println("User " + member.getEffectiveName() + " tried to use the queue command but was not in a voice channel");
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if(!selfVoiceState.inAudioChannel()) {
            event.reply("I am not in an audio channel").queue();
            System.out.println("User " + member.getEffectiveName() + " tried to use the queue command but I am not in an audio channel");
            return;
        }

        if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You are not in the same channel as me").queue();
            System.out.println("User " + member.getEffectiveName() + " tried to use the queue command but was not in the same voice channel as me");
            return;
        }

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(guildMusicManager.getTrackScheduler().getQueue());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Current Queue");

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("page_first", Emoji.fromUnicode("⏪")));
        buttons.add(Button.primary("page_previous", Emoji.fromUnicode("◀")));
        buttons.add(Button.danger("page_cancel", Emoji.fromUnicode("❌")));
        buttons.add(Button.primary("page_next", Emoji.fromUnicode("▶")));
        buttons.add(Button.primary("page_last", Emoji.fromUnicode("⏩")));

        int startIndex = 0;
        int endIndex = Math.min(startIndex + 10, queue.size());

        if(queue.isEmpty()) {
            embedBuilder.setDescription("Queue is empty");
            System.out.println("User " + member.getEffectiveName() + " tried to use the queue command but the queue is empty");
        }
        for(int i = startIndex; i < endIndex; i++) {
            AudioTrackInfo info = queue.get(i).getInfo();
            embedBuilder.addField(i+1 + " : " + info.title, "", false);
            System.out.println("User " + member.getEffectiveName() + " used the queue command");
        }

        InteractionHook hook = event.deferReply().complete();
        Message originalMessage = hook.sendMessageEmbeds(embedBuilder.build()).addActionRow(buttons).complete();
        event.getJDA().addEventListener(new ButtonClickEventListener(queue, originalMessage));
    }

    public static void updateEmbedWithCurrentPage(EmbedBuilder embedBuilder, List<AudioTrack> queue, int currentPage) {
        int startIndex = currentPage * 10;
        int endIndex = Math.min(startIndex + 10, queue.size());

        if(queue.isEmpty()) {
            embedBuilder.setDescription("Queue is empty");
        } else {
            for(int i = startIndex; i < endIndex; i++) {
                AudioTrackInfo info = queue.get(i).getInfo();
                embedBuilder.addField(i+1 + " : " + info.title, "", false);
            }
        }
    }
}