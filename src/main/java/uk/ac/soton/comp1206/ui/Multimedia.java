package uk.ac.soton.comp1206.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;

import java.io.File;

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Game.class);
    private static MediaPlayer audioPlayer;
    private static MediaPlayer musicPlayer;
    private static boolean running = false;

    /**
     * Plays background music that loops
     * @param music music name
     */
    public static void playMusic(String music) {
        if (running) {
            musicPlayer.stop();
            running = false;
        }
        Media backgroundMusic = new Media(new File("src/main/resources/music/" + music).toURI().toString());
        musicPlayer = new MediaPlayer(backgroundMusic);
        musicPlayer.play();
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        running = true;
        logger.info("Music playing: " + music);
    }

    /**
     * Plays audio effects
     * @param audio audio name
     */
    public static void playAudio(String audio) {
        Media backgroundMusic = new Media(new File("src/main/resources/sounds/" + audio).toURI().toString());
        audioPlayer = new MediaPlayer(backgroundMusic);
        audioPlayer.play();
        logger.info("Audio playing: " + audio);
    }

    public static void stopMusic() {
        musicPlayer.stop();
    }
}
