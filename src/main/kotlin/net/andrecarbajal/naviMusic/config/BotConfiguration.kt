package net.andrecarbajal.naviMusic.config

import club.minnced.discord.jdave.interop.JDaveSessionFactory
import jakarta.annotation.PreDestroy
import net.andrecarbajal.naviMusic.commands.SlashCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.audio.AudioModuleConfig
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import java.util.concurrent.TimeUnit

@Configuration
class BotConfiguration(
    private val slashCommands: List<SlashCommand>,
    private val listeners: List<ListenerAdapter>
) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(BotConfiguration::class.java)

    @Value("\${app.discord.token}")
    private lateinit var token: String

    private var jda: JDA? = null

    @EventListener(ContextRefreshedEvent::class)
    fun onApplicationEvent() {
        if (jda != null) return

        log.info("Starting Bot")

        if (token.isBlank() || token == "default") {
            log.error("Bot token not specified in environment / application.properties")
            return
        }

        try {
            jda = JDABuilder
                .createDefault(token)
                .setActivity(Activity.customStatus("Navi Music | /help"))
                .enableIntents(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_VOICE_STATES
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setAudioModuleConfig(AudioModuleConfig().withDaveSessionFactory(JDaveSessionFactory()))
                .build().awaitReady()

            jda?.let {
                it.addEventListener(this)
                listeners.forEach { listener ->
                    if (listener != this) {
                        it.addEventListener(listener)
                    }
                }
            }

            registerCommands()
            log.info("Bot started and commands registered")
        } catch (e: InterruptedException) {
            log.error("Error starting JDA instance", e)
            Thread.currentThread().interrupt()
        } catch (e: Exception) {
            log.error("Unexpected error during JDA initialization", e)
        }
    }

    private fun registerCommands() {
        val commands = mutableSetOf<CommandData>()
        slashCommands.forEach { slashCommand ->
            val data = Commands.slash(slashCommand.name, slashCommand.description)

            slashCommand.permission?.let {
                data.defaultPermissions = DefaultMemberPermissions.enabledFor(it)
            }
            data.setContexts(InteractionContextType.GUILD)
            data.addOptions(slashCommand.optionDataList)
            commands.add(data)
        }
        jda?.updateCommands()?.addCommands(commands)?.queue()
    }

    @Bean
    fun jdaInstance(): JDA? {
        return jda
    }

    @PreDestroy
    fun shutdown() {
        log.info("Shutting down")
        jda?.let {
            it.shutdown()
            try {
                if (!it.awaitShutdown(10, TimeUnit.SECONDS)) {
                    it.shutdownNow()
                }
            } catch (e: InterruptedException) {
                log.error("Error during bot shutdown", e)
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        val command = slashCommands.find { it.name.equals(e.name, ignoreCase = true) }
        command?.onCommand(e)
    }
}
