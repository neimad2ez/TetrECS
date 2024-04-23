package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    /**
     * Game class
     */
    protected Game game;
    /**
     * Rectangle used for timer
     */
    private Rectangle timerBar;
    /**
     * HBox to put on to mainpane
     */
    private HBox timerBox;

    /**
     * Current pieceboard to place down
     */
    protected PieceBoard currentPieceBoard;
    /**
     * Next pieceboard to place down
     */
    protected PieceBoard nextPieceBoard;
    /**
     * Gameboard variable
     */
    protected GameBoard board;
    /**
     * Current high score
     */

    public IntegerProperty highScore = new SimpleIntegerProperty();

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        int currentHighscore = getHighScore();
        highScore.set(currentHighscore);
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        // Top
        var top = new HBox();
        top.setSpacing(40);
        top.setAlignment(Pos.CENTER);
        BorderPane.setMargin(top, new Insets(50, 0, 0, 0));
        mainPane.setTop(top);

        //Title
        Image image = new Image(getClass().getResourceAsStream("/images/TetrECS.png"));
        ImageView title = new ImageView(image);
        title.setPreserveRatio(true);
        title.setFitHeight(100);

        //Score
        var scoreBox = new VBox();
        scoreBox.setAlignment(Pos.CENTER);
        var scoreText = new Text("Score");
        scoreText.getStyleClass().add("heading");
        var score = new Text();
        score.getStyleClass().add("score");
        //Binds score's textProperty to the IntegerProperty saved in Game class.
        score.textProperty().bind(game.score.asString());
        scoreBox.getChildren().addAll(scoreText, score);

        //Lives
        var livesBox = new VBox();
        livesBox.setAlignment(Pos.CENTER);
        var livesText = new Text("Lives");
        livesText.getStyleClass().add("heading");
        var lives = new Text();
        lives.getStyleClass().add("lives");
        lives.textProperty().bind(game.lives.asString());
        livesBox.getChildren().addAll(livesText, lives);


        top.getChildren().addAll(livesBox, title, scoreBox);

        //Right box
        var right = new VBox();
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(0, 0, 0, -45));
        right.setSpacing(20);
        //down, left, up right
        mainPane.setRight(right);

        //Left box
        var left = new VBox();
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(0, 0, 0, 50));
        left.setSpacing(20);
        mainPane.setLeft(left);

        //Multiplier text
        var multiplierText = new Text("Multiplier");
        multiplierText.getStyleClass().add("heading");

        //Multiplier
        var multiplier = new Text();
        multiplier.getStyleClass().add("hiscore");
        multiplier.textProperty().bind(game.multiplier.asString());

        //Multipler box
        var multiplierBox = new VBox();
        multiplierBox.setAlignment(Pos.CENTER);
        multiplierBox.getChildren().addAll(multiplierText, multiplier);

        //Current piece text
        var currentPieceText = new Text("Current Piece");
        currentPieceText.getStyleClass().add("heading");

        //Current piece
        currentPieceBoard = new PieceBoard(100,100);
        currentPieceBoard.blocks[1][1].center();
        currentPieceBoard.setOnMouseClicked(event ->{
            logger.info("Current Piece Rotated");
            rotate();
        });

        //Current piece box
        var currentPieceBox = new VBox();
        currentPieceBox.setAlignment(Pos.CENTER);
        currentPieceBox.getChildren().addAll(currentPieceText, currentPieceBoard);

        //Next piece
        nextPieceBoard = new PieceBoard(75,75);
        nextPieceBoard.setOnMouseClicked(event -> {
            logger.info("Pieces have been swapped");
            swapPiece();
        });

        //Next piece text
        var nextPieceText = new Text("Next Piece");
        nextPieceText.getStyleClass().add("heading");

        //Next piece box
        var nextPieceBox = new VBox();
        nextPieceBox.setAlignment(Pos.CENTER);
        nextPieceBox.getChildren().addAll(nextPieceText, nextPieceBoard);

        //Highscore text
        var highscoreText = new Text("High Score");
        highscoreText.getStyleClass().add("heading");

        //Highscore
        var highScoreNum = new Text();
        highScoreNum.getStyleClass().add("hiscore");
        highScoreNum.textProperty().bind(highScore.asString());

        //Highscore box
        var highscoreBox = new VBox();
        highscoreBox.setAlignment(Pos.CENTER);
        highscoreBox.getChildren().addAll(highscoreText, highScoreNum);

        //Level text
        var levelText = new Text("Level");
        levelText.getStyleClass().add("heading");

        //Level
        var level = new Text();
        level.getStyleClass().add("level");
        level.textProperty().bind(game.level.asString());

        //Level box
        var levelBox = new VBox();
        levelBox.setAlignment(Pos.CENTER);
        levelBox.getChildren().addAll(levelText, level);

        right.getChildren().addAll(currentPieceBox, nextPieceBox);
        left.getChildren().addAll(highscoreBox, levelBox, multiplierBox);

        //Made board into global variable
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        board.setPadding(new Insets(0, 70, 0, 30));
        mainPane.setCenter(board);

        //Places a piece if left click occurs
        root.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                //Handle block on gameboard grid being clicked
                board.setOnBlockClick(this::blockClicked);
            }
        });

        //Countdown bar
        timerBox = new HBox();
        timerBar = new Rectangle();
        timerBar.setHeight(10);
        timerBar.setWidth(gameWindow.getWidth());
        timerBar.setFill(Color.GREEN);
        timerBox.getChildren().addAll(timerBar);
        mainPane.setBottom(timerBox);

        Button button = new Button();
        button.setOnMouseClicked(event -> {
            gameOver();
        });
        left.getChildren().add(button);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
        //Checks if score is greater than high score, if greater, high score becomes current score
        if (game.score.getValue() > getHighScore()) {
            logger.info("New highscore: "+ highScore);
            highScore.set(game.score.getValue());
        }
    }

    public void nextPiece(GamePiece gp) {
        currentPieceBoard.setPiece(game.currentPiece);
        nextPieceBoard.setPiece(game.nextPiece);
    }

    /**
     * Causes the block to fade away when a line is completed
     * @param coordinates coordinates of the blocks which will disappear
     */

    public void fadeOut(HashSet<GameBlockCoordinate> coordinates) {
        logger.info("Fade out");
        //Fade out animation
        board.fadeOut(coordinates);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Method to rotate current piece
     */
    public void rotate() {
        logger.info("Block rotated");
        Multimedia.playAudio("rotate.wav");
        game.rotateCurrentPiece(game.currentPiece);
        currentPieceBoard.setPiece(game.currentPiece);
    }

    /**
     * Method which switches the current piece and the next piece to be displayed
     */
    public void swapPiece() {
        Multimedia.playAudio("pling.wav");
        game.swapCurrentPiece();
        currentPieceBoard.setPiece(game.currentPiece);
        nextPieceBoard.setPiece(game.nextPiece);
    }

    /**
     * Keyboard support for the game for example W moves up, A moves left etc.
     */
    public void keyboardSupport() {
        scene.setOnKeyPressed(event -> {
            if (event.equals(KeyCode.W)) {

            }
        });
    }

    /**
     * Game over sends you to scores screen
     */
    public void gameOver() {
        logger.info("Game over");
        timerBar.setWidth(0);
        timerBar.setHeight(0);
        timerBar.setOpacity(0);
        gameWindow.startScores(game);
    }

    public int getHighScore() {
        logger.info("Updated high score");
        try {
            var scoreFile = Paths.get("localScores.txt");
            List<String> scores = Files.readAllLines(scoreFile);
            String[] parts = scores.get(0).split(":");
            int highscore = Integer.valueOf(parts[1]);
            logger.info("High score is: " + highscore);
            return highscore;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Timer animation for timer bar which keeps track of time in tehg ame
     * @param time amount of time per round
     */
    public void timerAnimation(int time) {
        Timeline timeline = new Timeline();
        KeyValue start = new KeyValue(timerBar.widthProperty(), gameWindow.getWidth());
        KeyValue green = new KeyValue(timerBar.fillProperty(), Color.GREEN);
        KeyValue yellow = new KeyValue(timerBar.fillProperty(), Color.YELLOW);
        KeyValue red = new KeyValue(timerBar.fillProperty(), Color.RED);
        KeyValue end = new KeyValue(timerBar.widthProperty(), 0);
        timeline.getKeyFrames().add(new KeyFrame(new Duration(0), start));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(0), green));
        timeline.getKeyFrames().add(new KeyFrame(new Duration((float) time/2), yellow));
        timeline.getKeyFrames().add(new KeyFrame(new Duration((float) time * 3/4), red));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(time), end));
        timeline.play();
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.setOnGameLoop(this::timerAnimation);
        game.start();
        currentPieceBoard.setPiece(game.currentPiece);
        nextPieceBoard.setPiece(game.nextPiece);
        Multimedia.playMusic("game_start.wav");
        game.setOnLineCleared(this::fadeOut);
        game.setNextPieceListener(this::nextPiece);
        game.setOnGameOver(this::gameOver);
//        game.setOnGameLoop(new GameLoopListener(){} );
        scene.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                gameWindow.startMenu();
            }
        });
        scene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                logger.info("Rotated current piece using right click");
                rotate();
            }
        });
    }

}
