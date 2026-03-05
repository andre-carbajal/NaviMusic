package net.andrecarbajal.naviMusic.config

import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.andrecarbajal.naviMusic.commands.general.HelpCommand
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandConfig {

    @Bean
    fun helpCommand(slashCommands: MutableList<SlashCommand>): HelpCommand {
        val helpCommand = HelpCommand(slashCommands)
        slashCommands.add(helpCommand)
        return helpCommand
    }
}
