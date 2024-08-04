package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Queue implements ICommand {
    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Display list of songs in queue";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        return event.getClient().getSelf()
                .flatMap(user -> {
                    Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));
                    List<AudioTrack> musicQueue = GuildAudioManager.of(guildId).getScheduler().getQueue();

                    if (musicQueue.isEmpty()) {
                        if (GuildAudioManager.of(guildId).getPlayer().getPlayingTrack() == null)
                            return event.reply().withEmbeds(EmbedCreator.createEmbed("Queue Command", "No songs in queue!").build());

                        AudioTrackInfo current = GuildAudioManager.of(guildId).getPlayer().getPlayingTrack().getInfo();
                        return event.reply().withEmbeds(EmbedCreator.createEmbedSongs(
                                EmbedCreator.createEmbed("Queue Command"), current).build());
                    }

                    AudioTrackInfo current = GuildAudioManager.of(guildId).getPlayer().getPlayingTrack().getInfo();
                    long totalTime = current.length;
                    List<EmbedCreateSpec.Builder> paginationBuilder = new ArrayList<>();
                    for (int pagination = 0; pagination < Math.ceil((double) musicQueue.size() / 10); pagination++) {
                        EmbedCreateSpec.Builder builder = getQueueBase(event, musicQueue, current);

                        for (int i = 0; i < Math.min(
                                Math.min(musicQueue.size(), musicQueue.size() - (pagination * 10)), 10); i++) {
                            AudioTrackInfo track = musicQueue.get(i + (pagination * 10)).getInfo();
                            totalTime += track.length;
                            long min = (track.length / (1000 * 60)) % 60;
                            long sec = (track.length / 1000) % 60;
                            builder.addField(String.format("`%d.` `[%02d:%02d]` %s", i + 1, min, sec, track.title),
                                    "", false);
                        }
                        paginationBuilder.add(builder);
                    }

                    List<EmbedCreateSpec> pagination = getBuilders(totalTime, paginationBuilder, user);
                    Button nextBtn = Button.secondary("next", ReactionEmoji.unicode("⏭️"));
                    Button prevBtn = Button.secondary("prev", ReactionEmoji.unicode("⏮️"));
                    Button doneDtn = Button.secondary("done", ReactionEmoji.unicode("✅"));

                    AtomicInteger page = new AtomicInteger();
                    AtomicBoolean ifDone = new AtomicBoolean(false);
                    Mono<Void> buttonListener = event.getClient().on(ButtonInteractionEvent.class, buttonEvent -> {
                                String buttonId = buttonEvent.getCustomId();
                                if (buttonId.equals("next")) {
                                    page.addAndGet(1);
                                    return buttonEvent.reply().withEmbeds(pagination.get(page.get())).withComponents(
                                            ActionRow.of(prevBtn,
                                                    nextBtn.disabled(page.get() == pagination.size() - 1), doneDtn));
                                }
                                if (buttonId.equals("prev")) {
                                    page.decrementAndGet();
                                    return buttonEvent.reply().withEmbeds(pagination.get(page.get())).withComponents(
                                            ActionRow.of(prevBtn.disabled(page.get() == 0), nextBtn, doneDtn));
                                }

                                // When done button is pressed
                                ifDone.set(true);
                                return buttonEvent.reply().withEmbeds(EmbedCreator.createEmbed("Queue Command", "Exiting out of queue").build());
                            })
                            .timeout(Duration.ofSeconds(30))
                            .onErrorResume(ignore -> {
                                if (!ifDone.get())
                                    return event.editReply(String.format(
                                            "Timeout for queue, %s!",
                                            event.getInteraction().getMember().get()
                                                    .getNicknameMention())).then();
                                return Mono.empty();
                            }).then();

                    return event.reply()
                            .withEmbeds(pagination.getFirst())
                            .withComponents(ActionRow.of(prevBtn.disabled(),
                                    (page.get() == pagination.size() - 1 ? nextBtn.disabled() : nextBtn), doneDtn))
                            .then(buttonListener);
                });
    }

    private EmbedCreateSpec.Builder getQueueBase(ChatInputInteractionEvent event, List<AudioTrack> musicQueue,
                                                 AudioTrackInfo current) {
        return EmbedCreateSpec.builder()
                .title(String.format("`%d` Total Songs in Queue", musicQueue.size() + 1))
                .description(String.format("Current song: `%s`", current.title))
                .thumbnail((event.getInteraction().getMember().isPresent() ?
                        event.getInteraction().getMember().get().getAvatarUrl() : ""));
    }

    private static List<EmbedCreateSpec> getBuilders(
            long totalTime, List<EmbedCreateSpec.Builder> paginationsBuilder, User bot) {

        long totalHour = (totalTime / (1000 * 60 * 60)) % 24;
        long totalMin = (totalTime / (1000 * 60)) % 60;
        long totalSec = (totalTime / 1000) % 60;

        List<EmbedCreateSpec> pagination = new ArrayList<>();
        for (int i = 0; i < paginationsBuilder.size(); i++) {
            EmbedCreateSpec.Builder page = paginationsBuilder.get(i);
            page.footer(String.format("Total Time: [%02d:%02d:%02d] -- Page %d / %d",
                    totalHour, totalMin, totalSec, i + 1, paginationsBuilder.size()), bot.getAvatarUrl());
            pagination.add(page.build());
        }
        return pagination;
    }
}
