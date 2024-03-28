package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.HashSet;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

    /**
     * Current pieceboard to place down
     */
    protected PieceBoard currentPieceBoard;
    /**
     * Next pieceboard to place down
     */
    protected PieceBoard nextPieceBoard;
    protected GameBoard board;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
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
        top.setSpacing(100);
        top.setAlignment(Pos.CENTER);
        BorderPane.setMargin(top, new Insets(35, 0, 0, 0));
        mainPane.setTop(top);
        var title = new Text("TetrECS");
        title.getStyleClass().add("bigtitle");

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

        //Right
        var right = new VBox();
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(0, 0, 0, 0));
        mainPane.setRight(right);

        //Left
        var left = new VBox();
        left.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(0,0,0,0));
        mainPane.setRight(right);

        //Multiplier
        var multiplierText = new Text("Multiplier");
        multiplierText.getStyleClass().add("heading");
        var multiplier = new Text();
        multiplier.getStyleClass().add("hiscore");
        multiplier.textProperty().bind(game.multiplier.asString());

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

        //Next piece
        nextPieceBoard = new PieceBoard(75,75);
        nextPieceBoard.setOnMouseClicked(event -> {
            logger.info("Pieces have been swapped");
            swapPiece();
        });

        //Next piece text
        var nextPieceText = new Text("Next Piece");
        nextPieceText.getStyleClass().add("heading");


        right.getChildren().addAll(multiplierText, multiplier, currentPieceText, currentPieceBoard, nextPieceText, nextPieceBoard);

        //Made board into global variable
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Places a piece if left click occurs
        root.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                //Handle block on gameboard grid being clicked
                board.setOnBlockClick(this::blockClicked);
            }
        });

    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    public void nextPiece(GamePiece gp) {
        currentPieceBoard.setPiece(game.currentPiece);
        nextPieceBoard.setPiece(game.nextPiece);
    }

    public void fadeOut(HashSet<GameBlockCoordinate> coordinates) {
        logger.info("Fade out");
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
     * Game over sends you back to menu
     */
    public void gameOver() {
        logger.info("Menu booted up again");
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        currentPieceBoard.setPiece(game.currentPiece);
        nextPieceBoard.setPiece(game.nextPiece);
        Multimedia.playMusic("game_start.wav");
        game.setOnLineCleared(this::fadeOut);
        game.setNextPieceListener(this::nextPiece);
        game.setOnGameOver(this::gameOver);
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
