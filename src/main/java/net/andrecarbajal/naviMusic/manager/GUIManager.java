package net.andrecarbajal.naviMusic.manager;

import net.andrecarbajal.naviMusic.gui.ConsoleGUI;

import java.awt.*;

public class GUIManager {
    public void checkGui(Boolean guiAvailable){
        if (!guiAvailable) {
            System.setProperty("java.awt.headless", "true");
        }

        if (!GraphicsEnvironment.isHeadless()) {
            boolean isGuiVisible = guiAvailable;
            ConsoleGUI consoleGUI = new ConsoleGUI();
            consoleGUI.setVisible(isGuiVisible);
        }
    }
}
