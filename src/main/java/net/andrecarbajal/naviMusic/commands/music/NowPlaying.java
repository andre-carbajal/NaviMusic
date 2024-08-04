package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;

public class NowPlaying implements ICommand {
    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Will display the current playing song";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Member member = event.getInteraction().getMember().get();

        EmbedCreateSpec.Builder embed = EmbedCreator.createEmbed("Now Playing");

        return member.getVoiceState()
                .flatMap(VoiceState::getChannel)
                .flatMap(voiceChannel -> voiceChannel.isMemberConnected(event.getClient().getSelfId()))
                .defaultIfEmpty(false)
                .flatMap(isConnected -> {
                    if (isConnected) {
                        Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));
                        AudioPlayer player = GuildAudioManager.of(guildId).getPlayer();
                        AudioTrack track = player.getPlayingTrack();

                        if (track == null) {
                            return event.reply().withEmbeds(embed.description("No songs currently playing!").build());
                        }

                        return event.reply().withEmbeds(EmbedCreator.createEmbedSongs(embed, track.getInfo()).build());
                    }

                    return event.reply().withEmbeds(embed.description(String.format("Not in the same voice channel as %s!", member.getNicknameMention())).build());
                });
    }
}
