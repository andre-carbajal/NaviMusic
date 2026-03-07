package net.andrecarbajal.naviMusic.config

import club.minnced.discord.jdave.interop.JDaveSessionFactory
import jakarta.annotation.PreDestroy
import net.andrecarbajal.naviMusic.commands.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.audio.AudioModuleConfig
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.ListenerAdapter
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
    private val commandManager: CommandManager,
    private val listeners: List<ListenerAdapter>
) {

    private val log = LoggerFactory.getLogger(BotConfiguration::class.java)

    @Value($$"${app.discord.token}")
    private lateinit var token: String

    private var jda: JDA? = null

    @EventListener(ContextRefreshedEvent::class)
    fun onApplicationEvent() {
        if (jda != null) return

        log.info("Starting JDA Instance...")

        if (token.isBlank() || token == "default") {
            log.error("Bot token not specified!")
            return
        }

        try {
            val builder = JDABuilder.createDefault(token)
                .setActivity(Activity.customStatus("Navi Music | /help"))
                .enableIntents(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_VOICE_STATES
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setAudioModuleConfig(AudioModuleConfig().withDaveSessionFactory(JDaveSessionFactory()))

            builder.addEventListeners(commandManager)
            listeners.forEach { listener ->
                if (listener != commandManager) {
                    builder.addEventListeners(listener)
                }
            }

            jda = builder.build().awaitReady()

            jda?.let {
                commandManager.registerAll(it)
            }

            log.info("Bot is ready and commands are mapped!")
        } catch (e: Exception) {
            log.error("Fatal error starting bot", e)
        }
    }

    @Bean
    fun jdaInstance(): JDA? = jda

    @PreDestroy
    fun shutdown() {
        log.info("Closing JDA session...")
        jda?.let {
            it.shutdown()
            if (!it.awaitShutdown(10, TimeUnit.SECONDS)) {
                it.shutdownNow()
            }
        }
    }
}
