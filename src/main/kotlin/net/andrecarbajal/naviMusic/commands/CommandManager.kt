package net.andrecarbajal.naviMusic.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CommandManager(private val slashCommands: List<SlashCommand>) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(CommandManager::class.java)

    fun registerAll(jda: JDA) {
        log.info("Registering {} slash commands automagically", slashCommands.size)
        val commandsData = slashCommands.map { cmd ->
            Commands.slash(cmd.name, cmd.description).apply {
                cmd.permission?.let { defaultPermissions = DefaultMemberPermissions.enabledFor(it) }
                setContexts(InteractionContextType.GUILD)
                addOptions(cmd.optionDataList)
            }
        }
        jda.updateCommands().addCommands(commandsData).queue {
            log.info("Successfully registered {} commands in Discord", commandsData.size)
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        slashCommands.find { it.name.equals(event.name, ignoreCase = true) }?.let {
            log.debug("Executing command: /{}", it.name)
            it.onCommand(event)
        } ?: run {
            log.warn("Command not found: /{}", event.name)
        }
    }
}
