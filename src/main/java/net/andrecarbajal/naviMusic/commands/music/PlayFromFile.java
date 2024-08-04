package net.andrecarbajal.naviMusic.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.voice.AudioProvider;
import net.andrecarbajal.naviMusic.audio.GuildAudioManager;
import net.andrecarbajal.naviMusic.commands.ICommand;
import net.andrecarbajal.naviMusic.util.EmbedCreator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static net.andrecarbajal.naviMusic.Main.PLAYER_MANAGER;

public class PlayFromFile implements ICommand {
    @Override
    public String getName() {
        return "play-from-file";
    }

    @Override
    public String getCategory() {
        return "music";
    }

    @Override
    public String getDescription() {
        return "Plays a song from a file.";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        EmbedCreateSpec.Builder embed = EmbedCreator.createEmbed("PlayFromFile Command");

        return event.deferReply()
                .then(Mono.justOrEmpty(event.getInteraction().getMember()))
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(channel -> {
                    if (channel == null) {
                        String name = ((event.getInteraction().getMember().isPresent() ?
                                event.getInteraction().getMember().get().getNicknameMention() : "`You`"));
                        event.editReply().withEmbeds(embed.description(String.format("%s is not in any voice channels!", name)).build()).subscribe();
                        return;
                    }

                    var option = event.getOption("file")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asAttachment);

                    String searchQuery = option.get().getUrl();
                    String filename = option.get().getFilename();
                    System.out.println(searchQuery);

                    Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));
                    AudioProvider voice = GuildAudioManager.of(guildId).getProvider();

                    Join.autoDisconnect(channel, voice).and(loadItem(event, searchQuery, filename, embed)).subscribe();
                })
                .doOnError(t -> event.editReply().withEmbeds(embed.description("Something happened...").build()).subscribe())
                .then();
    }

    private Mono<Object> loadItem(ChatInputInteractionEvent event, String query, String filename, EmbedCreateSpec.Builder embed) {
        Snowflake guildId = event.getInteraction().getGuildId().orElse(Snowflake.of(0));

        return Mono.create(monoSink ->
                PLAYER_MANAGER.loadItem(query, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        event.editReply().withEmbeds(EmbedCreator.createEmbedSongs(embed, audioTrack.getInfo(), filename).build())
                                .subscribe();
                        play(guildId, audioTrack);
                        monoSink.success();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        if (!audioPlaylist.isSearchResult()) {
                            event.editReply().withEmbeds(EmbedCreator.createEmbedPlaylist(embed, query, audioPlaylist).build()).subscribe();
                            for (AudioTrack track : audioPlaylist.getTracks())
                                play(guildId, track);
                            monoSink.success();
                            return;
                        }
                        monoSink.success();
                    }

                    @Override
                    public void noMatches() {
                        event.editReply().withEmbeds(embed.description(String.format("Can't find match to `%s`", query)).build());
                        monoSink.error(new Exception("No match!"));
                    }

                    @Override
                    public void loadFailed(FriendlyException e) {
                        event.editReply().withEmbeds(embed.description(String.format("Can't play `%s`", query)).build());
                        monoSink.error(e);
                    }
                })
        );
    }

    private void play(Snowflake guildId, AudioTrack track) {
        GuildAudioManager.of(guildId).getScheduler().play(track);
    }

}
