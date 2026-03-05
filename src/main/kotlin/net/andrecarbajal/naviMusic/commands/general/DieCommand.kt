package net.andrecarbajal.naviMusic.commands.general

import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component

@Component
class DieCommand : SlashCommand("die", "Rolls a die", Category.GENERAL) {
    init {
        addOption(OptionData(OptionType.INTEGER, "sides", "Number of sides on the die", false))
    }

    override fun onCommand(event: SlashCommandInteractionEvent) {
        val member = event.member ?: return
        val sides = event.getOption("sides")?.asLong?.toInt() ?: 6
        val roll = (Math.random() * sides).toInt() + 1

        RichResponse.builder()
            .title("Roll of a $sides-sided die")
            .text("${member.effectiveName} rolled a $roll")
            .build()
            .sendReply(event)
    }
}
