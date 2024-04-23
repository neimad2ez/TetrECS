package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.MenuScene;

import java.util.ArrayList;

public class ScoresList extends VBox {
    private static final Logger logger = LogManager.getLogger(ScoresList.class);
    /**
     * Holds all the scores achieved
     */
    public SimpleListProperty<Pair<String, Integer>> scoresList = new SimpleListProperty<>();
//    private ArrayList<VBox> scores = new ArrayList<>();
    /**
     * Scores to be displayed
     */
    private ArrayList<Text> scores = new ArrayList<>();
    //Recreate list everytime a new score is added
    public ScoresList() {
        scoresList.addListener((ListChangeListener<Pair<String, Integer>>) change -> {
            createList();
        });
    }

    /**
     * Creates a scores list
     */
    public void createList() {
        logger.info("Creates a list");
        getChildren().clear();
        for (Pair<String, Integer> s: scoresList) {
            var text = new Text(s.getKey() + ": " + s.getValue());
            text.getStyleClass().add("heading");
            getChildren().add(text);
            scores.add(text);
            reveal();
        }
    }

    /**
     * Binds together the scoresList and the simple list property in the ScoreScene class
     * @param slp simpleListProperty
     */
    public void bind(SimpleListProperty<Pair<String, Integer>> slp) {
        scoresList.bind(slp);
        createList();
    }

    public void reveal() {
        int initialDelay = 0;
        for (Text text: scores) {
            text.setOpacity(0);
            FadeTransition fade = new FadeTransition(new Duration(1000), text);
            fade.setDelay(new Duration(initialDelay));
            fade.setFromValue(0);
            fade.setToValue(1);
            initialDelay += 200;
            fade.play();
        }

    }
}
