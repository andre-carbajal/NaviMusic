package net.andrecarbajal.naviMusic.commands.music;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.voice.VoiceConnection;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class Leave implements ICommand {
    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "The bot will disconnect from the current voice channel.";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Member member = event.getInteraction().getMember().get();

        EmbedCreateSpec.Builder embed = EmbedCreator.createEmbed("Leave Command");

        return member.getVoiceState()
                .flatMap(VoiceState::getChannel)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(voiceChannel -> {
                    if (voiceChannel == null) {
                        event.reply().withEmbeds(embed.description(String.format("%s must join a voice channel first!",
                                member.getNicknameMention())).build()).subscribe();
                        return;
                    }

                    voiceChannel.getVoiceConnection()
                            .publishOn(Schedulers.boundedElastic())
                            .doOnSuccess(voiceConnection -> {
                                if (voiceConnection == null) {
                                    voiceChannel.sendDisconnectVoiceState()
                                            .then(event.reply().withEmbeds(embed.description("Not in a voice channel!").build()))
                                            .subscribe();
                                }
                                Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));
                                GuildAudioManager.of(guildId).getPlayer().stopTrack();
                                GuildAudioManager.of(guildId).getScheduler().clear();
                            })
                            .flatMap(VoiceConnection::disconnect)
                            .then(event.reply().withEmbeds(embed.description(String.format("Leaving `\uD83d\uDD0A %s`!", voiceChannel.getName())).build()))
                            .subscribe();
                })
                .doOnError(t -> event.reply().withEmbeds(embed.description("Something happened...").build()))
                .then();
    }
}