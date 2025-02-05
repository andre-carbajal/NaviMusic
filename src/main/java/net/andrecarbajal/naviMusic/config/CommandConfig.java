package net.andrecarbajal.naviMusic.config;

import net.andrecarbajal.naviMusic.commands.SlashCommand;
import net.andrecarbajal.naviMusic.commands.general.HelpCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommandConfig {

    @Bean
    public HelpCommand helpCommand(List<SlashCommand> slashCommands) {
        HelpCommand helpCommand = new HelpCommand(slashCommands);
        slashCommands.add(helpCommand);
        return helpCommand;
    }
}
