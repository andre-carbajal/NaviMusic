package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.andrecarbajal.naviMusic.commands.CommandUtils;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.lavaplayer.GuildMusicManager;
import net.andrecarbajal.naviMusic.lavaplayer.PlayerManager;
import net.andrecarbajal.naviMusic.listener.ButtonClickEvent;
import net.dv8tion.jda.api.EmbedBuilder;
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
        Member self = event.getGuild().getSelfMember();

        if (!CommandUtils.validateVoiceState(event, member, self)) return;

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(guildMusicManager.getTrackScheduler().getQueue());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setThumbnail("https://i.imgur.com/xiiGqIO.png");
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
            event.getJDA().addEventListener(new ButtonClickEvent(queue, originalMessage));
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