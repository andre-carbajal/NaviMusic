package net.andrecarbajal.naviMusic.ui

import org.springframework.stereotype.Component
import java.awt.GraphicsEnvironment

@Component
class UIManager {
    fun checkGui(args: Array<String>) {
        var isGuiVisible = true

        if (args.any { it == "nogui" }) {
            System.setProperty("java.awt.headless", "true")
            isGuiVisible = false
        }

        if (!GraphicsEnvironment.isHeadless()) {
            val consoleUI = ConsoleUI()
            consoleUI.setVisible(isGuiVisible)
        }
    }
}
