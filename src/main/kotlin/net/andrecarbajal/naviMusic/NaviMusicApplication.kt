package net.andrecarbajal.naviMusic

import net.andrecarbajal.naviMusic.ui.UIManager
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class NaviMusicApplication

private val uiManager = UIManager()

fun main(args: Array<String>) {
    uiManager.checkGui(args)
    SpringApplication.run(NaviMusicApplication::class.java, *args)
}
