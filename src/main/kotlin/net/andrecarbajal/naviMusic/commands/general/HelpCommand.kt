package net.andrecarbajal.naviMusic.commands.general

import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.dto.response.RichResponse
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class HelpCommand(private val slashCommands: List<SlashCommand>) :
    SlashCommand("help", "Shows the list of commands and their descriptions", Category.GENERAL) {

    override fun onCommand(event: SlashCommandInteractionEvent) {
        val sb = StringBuilder()
        sb.append("**General Commands:**\n")
        slashCommands.stream().filter { slashCommand -> slashCommand.category == Category.GENERAL }
            .forEach { slashCommand ->
                sb.append("`").append("/").append(slashCommand.name).append("`").append(" - ")
                    .append(slashCommand.description).append("\n")
            }
        sb.append("\n")
        sb.append("**Music Commands:**\n")
        slashCommands.stream().filter { slashCommand -> slashCommand.category == Category.MUSIC }
            .forEach { slashCommand ->
                sb.append("`").append("/").append(slashCommand.name).append("`").append(" - ")
                    .append(slashCommand.description).append("\n")
            }
        RichResponse(sb.toString(), RichResponse.Type.OK, false).sendReply(event)
    }
}
