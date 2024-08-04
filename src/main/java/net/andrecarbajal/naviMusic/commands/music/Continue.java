package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;

public class Continue implements ICommand {
    @Override
    public String getName() {
        return "continue";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Continues the music";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Member member = event.getInteraction().getMember().get();

        return member.getVoiceState()
                .flatMap(VoiceState::getChannel)
                .flatMap(voiceChannel -> voiceChannel.isMemberConnected(event.getClient().getSelfId()))
                .defaultIfEmpty(false)
                .flatMap(isConnected -> {
                    EmbedCreateSpec.Builder embed = EmbedCreator.createEmbed("Continue Command");
                    if (isConnected) {
                        Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));
                        AudioPlayer player = GuildAudioManager.of(guildId).getPlayer();

                        if (!player.isPaused())
                            return event.reply().withEmbeds(embed.description("The music is already playing!").build());

                        player.setPaused(false);
                        return event.reply().withEmbeds(embed.description("The music is now playing!").build());

                    }
                    return event.reply().withEmbeds(
                            embed.description("You need to be in a voice channel with the bot to use this command!").build());
                });
    }
}
