package net.andrecarbajal.naviMusic.commands.music;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;

public class Skip implements ICommand {
    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Skip/remove first song from queue";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Member member = event.getInteraction().getMember().get();
        EmbedCreateSpec.Builder embed = EmbedCreator.createEmbed("Skip Command");

        return member.getVoiceState()
                .flatMap(VoiceState::getChannel)
                .flatMap(voiceChannel -> voiceChannel.isMemberConnected(event.getClient().getSelfId()))
                .defaultIfEmpty(false)
                .flatMap(isConnected -> {
                    if (isConnected) {
                        Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));
                        int position = Math.toIntExact(event.getOption("position")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asLong).orElse(0L));

                        if (position == 0) {
                            if (GuildAudioManager.of(guildId).getPlayer().getPlayingTrack() == null)
                                return event.reply().withEmbeds(embed.description("No song is currently playing!").build());
                            if (GuildAudioManager.of(guildId).getScheduler().skip())
                                return event.reply().withEmbeds(embed.description("Skipping to next song!").build());
                            GuildAudioManager.of((guildId)).getPlayer().stopTrack();
                            return event.reply().withEmbeds(embed.description("Skipping current song!").build());
                        }

                        GuildAudioManager.of(guildId).getScheduler().skip(position - 1);
                        return event.reply().withEmbeds(embed.description(String.format("Skipping song at position `%d` in queue!", position)).build());
                    }

                    return event.reply().withEmbeds(embed.description("Not in the same voice channel!").build());
                });
    }
}
