package net.andrecarbajal.naviMusic.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.andrecarbajal.naviMusic.dto.response.Response;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class SlashCommand {
    private final String name;
    private final String description;
    private final Category category;
    private Permission permission;
    private List<OptionData> optionDataList = new ArrayList<>();

    protected enum Category {
        GENERAL,
        MUSIC
    }

    protected void addOption(OptionData optionData) {
        optionDataList.add(optionData);
    }

    public abstract void onCommand(SlashCommandInteractionEvent event);

    protected boolean noVoiceChannelCheck(SlashCommandInteractionEvent e) {
        if (e.getGuild().getVoiceChannels().parallelStream().filter(vc -> vc.getMembers().contains(e.getMember())).findFirst().isEmpty()) {
            RichResponse response = new RichResponse();
            response.setEphimeral(true);
            response.setType(Response.Type.USER_ERROR);
            response.setTitle("You must be in a voice channel to use this command");

            response.sendReply(e);
            return true;
        }

        return false;
    }
}
