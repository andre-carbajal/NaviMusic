package net.andrecarbajal.naviMusic;

import net.andrecarbajal.naviMusic.manager.GUIManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NaviMusicApplication {
    private static final GUIManager guiManager = new GUIManager();

    public static void main(String[] args) {
        guiManager.checkGui(args);

        SpringApplication.run(NaviMusicApplication.class, args);
    }
}