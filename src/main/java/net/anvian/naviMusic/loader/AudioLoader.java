package net.anvian.naviMusic.loader;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.*;
import net.anvian.naviMusic.MyUserData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AudioLoader extends AbstractAudioLoadResultHandler {
    private final Link link;
    private final SlashCommandInteractionEvent event;

    public AudioLoader(Link link, SlashCommandInteractionEvent event) {
        this.link = link;
        this.event = event;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();

        var userData = new MyUserData(event.getUser().getIdLong());

        track.setUserData(userData);

        link.createOrUpdatePlayer().setTrack(track).setVolume(35).subscribe((player) -> {
            final Track playingTrack = player.getTrack();
            final var trackTitle = playingTrack.getInfo().getTitle();
            final MyUserData customData = playingTrack.getUserData(MyUserData.class);

            event.getHook().sendMessage("Now playing: " + trackTitle + "\nRequested by: <@" + customData.requester() + '>').queue();
        });
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result) {
        final int trackCount = result.getTracks().size();
        event.getHook().sendMessage("This playlist has " + trackCount + " tracks!").queue();
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result) {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            event.getHook().sendMessage("No tracks found!").queue();
            return;
        }

        final Track firstTrack = tracks.get(0);

        link.updatePlayer((update) -> update.setTrack(firstTrack).setVolume(35)).subscribe((ignored) -> {
            event.getHook().sendMessage("Now playing: " + firstTrack.getInfo().getTitle()).queue();
        });
    }

    @Override
    public void noMatches() {
        event.getHook().sendMessage("No matches found for your input!").queue();
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result) {
        event.getHook().sendMessage("Failed to load track! " + result.getException().getMessage()).queue();
    }
}