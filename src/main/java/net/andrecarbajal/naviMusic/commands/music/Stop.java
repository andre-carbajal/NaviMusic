package net.andrecarbajal.naviMusic.commands.music;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;

public class Stop implements ICommand {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Stop playing and clear queue";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Member member = event.getInteraction().getMember().get();
        EmbedCreateSpec.Builder embed = EmbedCreator.createEmbed(" Stop Command");

        return member.getVoiceState()
                .flatMap(VoiceState::getChannel)
                .flatMap(voiceChannel -> voiceChannel.isMemberConnected(event.getClient().getSelfId()))
                .defaultIfEmpty(false)
                .flatMap(isConnected -> {
                    if (isConnected) {
                        Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));
                        GuildAudioManager.of(guildId).getPlayer().stopTrack();
                        GuildAudioManager.of(guildId).getScheduler().clear();
                        return event.reply().withEmbeds(embed.description("Stopped and cleared queue!").build());
                    }

                    return event.reply().withEmbeds(embed.description("Not in the same voice channel!").build());
                });
    }
}