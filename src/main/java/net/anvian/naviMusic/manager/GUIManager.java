package net.anvian.naviMusic.manager;

import net.anvian.naviMusic.gui.ConsoleGUI;

import java.awt.*;

public class GUIManager {
    public void checkGui(String guiOption){
        if (guiOption.equals("nogui")) {
            System.setProperty("java.awt.headless", "true");
        }

        if (!GraphicsEnvironment.isHeadless()) {
            boolean isGuiVisible = !guiOption.equals("nogui");
            ConsoleGUI consoleGUI = new ConsoleGUI();
            consoleGUI.setVisible(isGuiVisible);
        }
    }
}
