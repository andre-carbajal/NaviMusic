package net.andrecarbajal.naviMusic.commands.music;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.voice.AudioProvider;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class Join implements ICommand {
    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Join your current voice channel";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Member member = event.getInteraction().getMember().get();

        EmbedCreateSpec.Builder embed = EmbedCreator.createEmbed("Join Command");

        return member.getVoiceState()
                .flatMap(VoiceState::getChannel)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(voiceChannel -> {
                    if (voiceChannel == null) {
                        event.reply().withEmbeds(embed.description(String.format("%s must join a voice channel first!",
                                member.getNicknameMention())).build()).subscribe();
                        return;
                    }
                    AudioProvider voice = GuildAudioManager.of(member.getGuildId()).getProvider();
                    event.reply().withEmbeds(embed.description(String.format("Joining `\uD83d\uDD0A %s`!", voiceChannel.getName())).build())
                            .then(autoDisconnect(voiceChannel, voice)).subscribe();
                })
                .doOnError(t -> event.reply().withEmbeds(embed.description("Something happened...").build()).subscribe())
                .then();
    }

    public static Mono<Void> autoDisconnect(VoiceChannel channel, AudioProvider voice) {
        return channel.join().withProvider(voice)
                .flatMap(voiceConnection -> {
                    // The bot itself has a VoiceState; 1 VoiceState signals bot is alone
                    Publisher<Boolean> voiceStateCounter = channel.getVoiceStates()
                            .count()
                            .map(count -> 1L == count);

                    // After 5 seconds, check if the bot is alone. This is useful if
                    // the bot joined alone, but no one else joined since connecting
                    Mono<Void> onDelay = Mono.delay(Duration.ofSeconds(5L))
                            .filterWhen(ignored -> voiceStateCounter)
                            .switchIfEmpty(Mono.never())
                            .then();

                    // As people join and leave `channel`, check if the bot is alone.
                    Mono<Void> onEvent = channel.getClient().getEventDispatcher().on(VoiceStateUpdateEvent.class)
                            .filter(event -> event.getOld().flatMap(VoiceState::getChannelId)
                                    .map(channel.getId()::equals).orElse(false))
                            .filterWhen(ignored -> voiceStateCounter)
                            .next()
                            .then();

                    // Disconnect the bot if either onDelay or onEvent are completed!
                    return onDelay.or(onEvent).then(voiceConnection.disconnect());
                });
    }
}