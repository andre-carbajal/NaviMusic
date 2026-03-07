package net.andrecarbajal.naviMusic

import net.andrecarbajal.naviMusic.ui.UIManager
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext

@SpringBootApplication
class NaviMusicApplication

fun main(args: Array<String>) {
    val context: ApplicationContext = SpringApplication.run(NaviMusicApplication::class.java, *args)

    val uiManager = context.getBean<UIManager>()
    uiManager.checkGui(args)
}
