package net.anvian.naviMusic.manager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VoiceChannelManager {
    private void disconnectBot(JDA jda) {
        List<Guild> guilds = jda.getGuilds();

        for (Guild guild : guilds) {
            List<VoiceChannel> voiceChannels = guild.getVoiceChannels();

            for (VoiceChannel voiceChannel : voiceChannels) {
                List<Member> members = voiceChannel.getMembers();

                if (members.isEmpty() || (members.size() == 1 && members.get(0).getUser().isBot())) {
                    AudioManager audioManager = guild.getAudioManager();
                    audioManager.closeAudioConnection();
                }
            }
        }
    }

    public void scheduleDisconnect(JDA jda) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> disconnectBot(jda), 0, 3, TimeUnit.MINUTES);
    }

}
