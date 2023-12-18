package net.anvian.naviMusic.listener;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.anvian.naviMusic.commands.music.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class ButtonClickEventListener extends ListenerAdapter {
    List<AudioTrack> queue;
    int currentPage = 0;
    Message originalMessage;

    public ButtonClickEventListener(List<AudioTrack> queue, Message originalMessage) {
        this.queue = queue;
        this.originalMessage = originalMessage;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        List<Button> buttons = new ArrayList<>();

        switch (buttonId) {
            case "page_first":
                currentPage = 0;
                break;
            case "page_previous":
                if (currentPage > 0) {
                    currentPage--;
                }
                break;
            case "page_next":
                if (currentPage < queue.size() / 10) {
                    currentPage++;
                }
                break;
            case "page_last":
                currentPage = queue.size() / 10;
                break;
            case "page_cancel":
                originalMessage.delete().queue();
                return;
        }

        buttons.add(Button.primary("page_first", Emoji.fromUnicode("⏪")));
        buttons.add(Button.primary("page_previous", Emoji.fromUnicode("◀")));
        buttons.add(Button.danger("page_cancel", Emoji.fromUnicode("❌")));
        buttons.add(Button.primary("page_next", Emoji.fromUnicode("▶")));
        buttons.add(Button.primary("page_last", Emoji.fromUnicode("⏩")));

        EmbedBuilder msg = new EmbedBuilder();
        updateEmbedWithCurrentPage(msg, queue, currentPage);
        originalMessage.delete().queue();
        originalMessage = event.getChannel().sendMessageEmbeds(msg.build()).setActionRow(buttons).complete();
    }

    private void updateEmbedWithCurrentPage(EmbedBuilder embedBuilder, List<AudioTrack> queue, int currentPage) {
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