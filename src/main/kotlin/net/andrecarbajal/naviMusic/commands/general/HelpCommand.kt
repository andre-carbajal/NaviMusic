package net.andrecarbajal.naviMusic.commands.general

import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class HelpCommand(private val slashCommands: List<SlashCommand>) :
    SlashCommand("help", "Shows the list of commands and their descriptions", Category.GENERAL) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        val sb = StringBuilder()
        sb.append("**General Commands:**\n")
        slashCommands.filter { it.category == Category.GENERAL }
            .forEach { slashCommand ->
                sb.append("`").append("/").append(slashCommand.name).append("`").append(" - ")
                    .append(slashCommand.description).append("\n")
            }
        sb.append("\n**Music Commands:**\n")
        slashCommands.filter { it.category == Category.MUSIC }
            .forEach { slashCommand ->
                sb.append("`").append("/").append(slashCommand.name).append("`").append(" - ")
                    .append(slashCommand.description).append("\n")
            }
        event.reply(sb.toString()).queue()
    }
}
