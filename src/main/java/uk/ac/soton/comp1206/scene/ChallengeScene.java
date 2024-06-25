package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import uk.ac.soton.comp1206.event.ModeListener;
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
    private Game soloGame;
    /**
     * Rectangle used for timer
     */
    public Rectangle timerBar;
    /**
     * HBox to put on to mainpane
     */
    public HBox timerBox;

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
     * Current game mode
     */
    public String mode;


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow, String mode) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
        this.mode = mode;
        int currentHighscore = getHighScore();
        highScore.set(currentHighscore);
    }

    public void setUpGame() {
        soloGame = new Game(5,5);
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        sendMode();

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
        score.textProperty().bind(soloGame.score.asString());
        scoreBox.getChildren().addAll(scoreText, score);

        //Lives
        var livesBox = new VBox();
        livesBox.setAlignment(Pos.CENTER);
        var livesText = new Text("Lives");
        livesText.getStyleClass().add("heading");
        var lives = new Text();
        lives.getStyleClass().add("lives");
        lives.textProperty().bind(soloGame.lives.asString());
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
        multiplier.textProperty().bind(soloGame.multiplier.asString());

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
        level.textProperty().bind(soloGame.level.asString());

        //Level box
        var levelBox = new VBox();
        levelBox.setAlignment(Pos.CENTER);
        levelBox.getChildren().addAll(levelText, level);

        right.getChildren().addAll(currentPieceBox, nextPieceBox);
        left.getChildren().addAll(highscoreBox, levelBox, multiplierBox);

        //Made board into global variable
        board = new GameBoard(soloGame.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
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
    }

    /**
     * Send mode to Game class
     */
    private void sendMode() {
        soloGame.select(mode);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    void blockClicked(GameBlock gameBlock) {
        soloGame.blockClicked(gameBlock);
        //Checks if score is greater than high score, if greater, high score becomes current score
        if (soloGame.score.getValue() > getHighScore()) {
            logger.info("New highscore: "+ highScore);
            highScore.set(soloGame.score.getValue());
        }
    }

    /**
     * Next piece
     * @param gp GamePiece
     */
    public void nextPiece(GamePiece gp) {
        currentPieceBoard.setPiece(soloGame.currentPiece);
        nextPieceBoard.setPiece(soloGame.nextPiece);
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
        soloGame = new Game(5, 5);
    }

    /**
     * Method to rotate current piece
     */
    public void rotate() {
        logger.info("Block rotated");
        Multimedia.playAudio("rotate.wav");
        soloGame.rotateCurrentPiece(soloGame.currentPiece);
        currentPieceBoard.setPiece(soloGame.currentPiece);
    }

    /**
     * Method which switches the current piece and the next piece to be displayed
     */
    public void swapPiece() {
        Multimedia.playAudio("pling.wav");
        soloGame.swapCurrentPiece();
        currentPieceBoard.setPiece(soloGame.currentPiece);
        nextPieceBoard.setPiece(soloGame.nextPiece);
    }

    /**
     * Keyboard support for the game for example W moves up, A moves left etc.
     */
    public void keyboardSupport(KeyEvent key) {
        logger.info("Key is pressed");
        var keyboard = key.getCode();

        int x = board.getHoveredBlock().getX();
        int y = board.getHoveredBlock().getY();

        //Place piece
        if (keyboard.equals(KeyCode.ENTER) || keyboard.equals(KeyCode.X)) {
            blockClicked(board.getBlock(x,y));
        }

        //Move up
        if (keyboard.equals(KeyCode.W) || keyboard.equals(KeyCode.UP) && y > 0) {
            y--;
        }

        //Move left
        if (keyboard.equals(KeyCode.A) || keyboard.equals(KeyCode.LEFT) && x > 0) {
            x--;
        }

        //Move down
        if (keyboard.equals(KeyCode.S) || keyboard.equals(KeyCode.DOWN) && y < 5) {
            y++;
        }

        //Move right
        if (keyboard.equals(KeyCode.D) || keyboard.equals(KeyCode.RIGHT) && x < 5) {
            x++;
        }

        //Swap pieces
        if (keyboard.equals(KeyCode.SPACE) || keyboard.equals(KeyCode.R) && x > 0) {
            swapPiece();
        }

        //Left rotation
        if (keyboard.equals(KeyCode.Q) || keyboard.equals(KeyCode.Z) || keyboard.equals(KeyCode.OPEN_BRACKET)) {
            for (int i = 0; i < 3; i++) {
                rotate();
            }
        }

        //Rotate right
        if (keyboard.equals(KeyCode.E) || keyboard.equals(KeyCode.C) || keyboard.equals(KeyCode.CLOSE_BRACKET)) {
            rotate();
        }

        //Discard block and reset game loop
        if (keyboard.equals(KeyCode.V)) {
            soloGame.gameLoop();
        }

        //Clear all hovered blocks
        board.resetHover();

        // Update the new hovered block
        GameBlock newHoveredBlock = board.getBlock(x, y);
        board.hover(newHoveredBlock);
    }

    /**
     * Game over sends you to scores screen
     */
    public void gameOver() {
        logger.info("Game over");
        timerBar.setWidth(0);
        timerBar.setHeight(0);
        timerBar.setOpacity(0);
        gameWindow.startScores(soloGame);
    }

    /**
     * Reads localScores.txt file and extracts data to find high scores
     * @return
     */
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
        //When gameloop is reset or ends, timer is updated and getTimerDelay is passed in
        soloGame.setOnGameLoop(this::timerAnimation);
        soloGame.start();
        currentPieceBoard.setPiece(soloGame.currentPiece);
        nextPieceBoard.setPiece(soloGame.nextPiece);
        Multimedia.playMusic("game_start.wav");
        //Links fadeOut method with lineCleared in interface due to same method signature.
        soloGame.setOnLineCleared(this::fadeOut);
        soloGame.setNextPieceListener(this::nextPiece);
        soloGame.setOnGameOver(this::gameOver);
        scene.setOnKeyPressed(event -> {
            logger.info("This one works");
            keyboardSupport(event);
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                gameWindow.startMenu();
                soloGame.shutdown();
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
