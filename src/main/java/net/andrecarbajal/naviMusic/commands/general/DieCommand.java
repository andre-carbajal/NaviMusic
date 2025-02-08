package net.andrecarbajal.naviMusic.commands.general;

import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.dto.response.RichResponse;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

@Component
public class DieCommand extends SlashCommand {
    public DieCommand() {
        super("die", "Rolls a die", Category.GENERAL);
        addOption(new OptionData(OptionType.INTEGER, "sides", "Number of sides on the die", false));
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        assert member != null;

        int sides = event.getOption("sides") == null ? 6 : (int) event.getOption("sides").getAsLong();
        int roll = (int) (Math.random() * sides) + 1;

        RichResponse.builder()
                .title(String.format("Roll of a %d-sided die", sides))
                .text(String.format("%s rolled a %d", member.getEffectiveName(), roll))
                .build()
                .sendReply(event);
    }
}
