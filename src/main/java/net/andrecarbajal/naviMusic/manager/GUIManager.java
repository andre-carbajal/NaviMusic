package net.andrecarbajal.naviMusic.manager;

import net.andrecarbajal.naviMusic.gui.ConsoleGUI;

import java.awt.*;

public class GUIManager {
    public void checkGui(String[] args) {
        boolean isGuiVisible = true;

        for (String arg : args) {
            if (arg.equals("nogui")) {
                System.setProperty("java.awt.headless", "true");
                isGuiVisible = false;
                break;
            }
        }

        if (!GraphicsEnvironment.isHeadless()) {
            ConsoleGUI consoleGUI = new ConsoleGUI();
            consoleGUI.setVisible(isGuiVisible);
        }
    }
}