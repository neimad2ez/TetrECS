package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.collections.FXCollections;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ScoreScene extends BaseScene{
    private final Game game1;
    Communicator com;
//    private SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(FXCollections.observableArrayList());
    /**
     * Holds the score list to be imported
     */
    private ObservableList<Pair<String, Integer>> localScores;
    private ObservableList<Pair<String, Integer>> onlineScores;
    /**
     * ScoreList variable for local scores
     */
    private ScoresList scoreList;
    /**
     * ScoreList variable for online scores
     */
    private ScoresList onlineScoreList;

    private static final Logger logger = LogManager.getLogger(ScoreScene.class);
    private Text test;
    private Text test1;

    /**
     * Create a new score scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public ScoreScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game1 = game;
        scoreList = new ScoresList();
        onlineScoreList = new ScoresList();
        com = gameWindow.getCommunicator();
        logger.info("Creating Score Scene");
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        logger.info("Initializing " + this.getClass().getName());
        Multimedia.playMusic("end.wav");
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
            }
        });
    }

    /**
     * Loads scores
     * @return ArrayList of the sorted scores in format name:score
     */
    public ArrayList<Pair<String, Integer>> loadScores() {
        logger.info("Loading scores");
        ArrayList<Pair<String,Integer>> score = new ArrayList<>();
        File file = new File("localScores.txt");
        if (!file.exists()) {
            ArrayList<Pair<String, Integer>> tempScores = new ArrayList<>();
            tempScores.add(new Pair<>("Guest", 1000));
            tempScores.add(new Pair<>("Guest", 500));
            tempScores.add(new Pair<>("Guest", 250));
            tempScores.add(new Pair<>("Guest", 200));
            tempScores.add(new Pair<>("Guest", 180));
            tempScores.add(new Pair<>("Guest", 150));
            tempScores.add(new Pair<>("Guest", 100));
            tempScores.add(new Pair<>("Guest", 50));
            tempScores.add(new Pair<>("Guest", 20));
            writeScores(tempScores);
        }
        try {
            logger.info("Retrieving scores");
            var scoreFile = Paths.get("localScores.txt");
            //Creates a list of the lines of the file
            List<String> scores = Files.readAllLines(scoreFile);
            for (String s: scores) {
                s = s.trim();
                String[] parts = s.split(":");
                //Split into name and points
                score.add(new Pair<>(parts[0], Integer.valueOf(parts[1])));
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return score;
    }

    /**
     * writeScores for
     * @param scores
     */
    public void writeScores(ArrayList<Pair<String, Integer>> scores) {
        int count = 0;
        try {
            // Sort the scores based on the integer values in descending order
            Collections.sort(scores, (pair1, pair2) -> pair2.getValue().compareTo(pair1.getValue()));
            BufferedWriter bf = new BufferedWriter(new FileWriter("localScores.txt"));
            for (Pair<String,Integer> pair: scores) {
                bf.write(pair.getKey()+":"+pair.getValue());
                count++;
                if (count == 10) {
                    break;
                }
            }
            bf.close();
            logger.info("Scores saved");
        } catch (IOException e) {
            logger.info("Scores saved error");
            throw new RuntimeException(e);
        }
    }

    /**
     * writeScores for localScoreList
     */
    public void writeScores() {
        int count = 0;
        try {
            Collections.sort(localScores, (pair1, pair2) -> pair2.getValue().compareTo(pair1.getValue()));
            BufferedWriter bf = new BufferedWriter(new FileWriter("localScores.txt"));
            for (Pair<String,Integer> pair: localScores) {
                bf.write(pair.getKey()+":"+pair.getValue()+"\n");
                count++;
                if (count == 10) {
                    break;
                }
            }
            bf.close();
            logger.info("Scores saved");
        } catch (IOException e) {
            logger.info("Scores saved error");
            throw new RuntimeException(e);
        }
    }

    public void checkHighscore() {
        int score = game1.score.get();
        logger.info("Scores: " + score);
        var name = new TextInputDialog("Enter your name");
        Boolean highscore = false;
        List<Pair<String,Integer>> newHighScores = new ArrayList<>();
        for (Pair<String, Integer> pair: localScores) {
            if (!highscore) {
                if (score > pair.getValue()) {
                    highscore = true;
                    Optional<String> inputtedName = name.showAndWait();
                    if (inputtedName.isPresent()) {
                        newHighScores.add(new Pair<>(inputtedName.get(), score));
                    }
                }
            }
        }
        localScores.addAll(newHighScores);
        writeScores();
        loadScores();
    }

    /**
     * Loads online high scores form server
     * @param message List of high scores form server
     */
    public void loadOnlineScores(String message) {
        logger.info("Retrieving online scores");
        String[] data = message.split(" ", 2);
        String scores = data[1];
        ArrayList<Pair<String, Integer>> tempOnlineScores = new ArrayList<>();
        String[] score = scores.split("\n");
        for (String s: score) {
            String[] parts = s.split(":");
            tempOnlineScores.add(new Pair<>(parts[0], Integer.valueOf(parts[1])));
        }
        onlineScores = FXCollections.observableArrayList(tempOnlineScores);
        onlineScoreList.scoresList.set(onlineScores);
        onlineScoreList.createList();
    }


    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        com.addListener(this::loadOnlineScores);
        com.send("HISCORES");

        localScores = FXCollections.observableArrayList(loadScores());

        checkHighscore();

        scoreList.scoresList.set(localScores);
        scoreList.setAlignment(Pos.CENTER);
        onlineScoreList.setAlignment(Pos.CENTER);

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scorePane = new BorderPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("instruction-background");
        root.getChildren().add(scorePane);

        //Game over heading
        var gameOver = new Text("Game Over");
        gameOver.getStyleClass().add("bigtitle");

        //Top box
        var topBox = new HBox(gameOver);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(40, 0, 0, 0));

        //Centre box
        var centerBox = new HBox();

        //Online high score box
        var onlineScoreBox = new VBox();
        onlineScoreBox.setAlignment(Pos.CENTER);

        //Online high score heading
        var onlineHighScore = new Text("Online High Scores");
        onlineHighScore.getStyleClass().add("title");
        onlineScoreBox.getChildren().add(onlineHighScore);
        onlineScoreBox.getChildren().add(onlineScoreList);

        //Local high score box
        var localScoreBox = new VBox();
        localScoreBox.setAlignment(Pos.CENTER);

        //Local high score heading
        var highScore = new Text("Local High Scores");
        highScore.getStyleClass().add("title");
        localScoreBox.getChildren().add(highScore);
        localScoreBox.getChildren().add(scoreList);

        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(30);
        centerBox.getChildren().addAll(localScoreBox, onlineScoreBox);

        //Retry
        var retry = new Text("Retry");
        retry.getStyleClass().add("menuItem");
        retry.setOnMouseClicked(event -> {
            gameWindow.startChallenge();
        });

        //Main menu
        var mainMenu = new Text("Main Menu");
        mainMenu.getStyleClass().add("menuItem");
        mainMenu.setOnMouseClicked(event -> {
            gameWindow.startMenu();
        });

        //Bottom box for main menu and retry
        var bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(0,0,30,0));
        bottomBox.setSpacing(100);
        bottomBox.getChildren().addAll(retry, mainMenu);

        scorePane.setTop(topBox);
        scorePane.setCenter(centerBox);
        scorePane.setBottom(bottomBox);
    }
}
