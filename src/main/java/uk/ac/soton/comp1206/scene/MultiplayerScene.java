package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Multiplayer scene, scene of the game where you can compete against other players

 */
public class MultiplayerScene extends ChallengeScene{
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    /**
     * Communicator variable
     */
    Communicator com;
    /**
     * Textfield for chat
     */
    TextField sendChat;
    /**
     * Timer to run tasks repeatedly
     */
    Timer timer;
    /**
     * Updates leaderboard repeatedly
     */
    Timer leaderboardTimer;
    /**
     * VBox for the leaderboard on the left side of the game
     */
    VBox leaderboard;
    /**
     *
     */
    Text leaderboardTitle;
    /**
     * userScores, username:score
     */
    ArrayList<Pair<String, Pair<String, Integer>>> userScores;
    /**
     * Game variable
     */
    private Game multiGame;
    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow, String mode) {
        super(gameWindow, mode);
        com = gameWindow.getCommunicator();
        userScores = new ArrayList<>();
    }

    /**
     * Initialise the game
     */
    @Override
    public void initialise() {
        requestScores();
        updateScores();
//        leaderboardTimer();

        multiGame.setOnGameLoop(this::timerAnimation);
        Multimedia.playMusic("game_start.wav");
        multiGame.setOnLineCleared(this::fadeOut);
        multiGame.setNextPieceListener(this::nextPiece);
        multiGame.setOnGameOver(this::gameOver);

        multiGame.start();

        currentPieceBoard.setPiece(multiGame.currentPiece);
        nextPieceBoard.setPiece(multiGame.nextPiece);

        scene.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                gameWindow.startMenu();
                com.send("DIE");
            }
        });

    }

    /**
     * Set up MultiplayerGame variable
     */
    @Override
    public void setUpGame() {
        multiGame = new MultiplayerGame(com, 5, 5);
    }

    /**
     * Next piece
     * @param gp GamePiece
     */
    @Override
    public void nextPiece(GamePiece gp) {
        currentPieceBoard.setPiece(multiGame.currentPiece);
        nextPieceBoard.setPiece(multiGame.nextPiece);
    }

    /**
     * Timer task that sends request to server for scores
     */
    public void requestScores() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                com.send("SCORES");
            }
        }, 0, 2000);
    }

    /**
     * Updates leaderboard using a timer task
     */
//    public void leaderboardTimer() {
//        leaderboardTimer = new Timer();
//        leaderboardTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                updateScores();
//            }
//        }, 0, 2000);
//    }

    /**
     * Updates scores
     */
    public void updateScores() {
        com.addListener(message -> {
            if (message.startsWith("SCORES")) {
                userScores.clear();
                //SCORES <Player>:<Score>:<Lives|DEAD>\n<Player>:<Score>:<Lives|DEAD>\n
                String[] components = message.split(" ");
                //<Player>:<Score>:<Lives|DEAD>
                String[] playerInfo = components[1].split("\n");
                for (String player: playerInfo) {
                    //<Player> <Score> <Lives|DEAD>
                    String[] score = player.split(":");
                    //Player, Lives|Dead, Scores
                    userScores.add(new Pair<>(score[0], new Pair<>(score[2], Integer.valueOf(score[1]))));
                    Platform.runLater(() -> updateLeaderboard());
                }
            }
        });
    }

    /**
     * Updates leaderboard when competing against other players
     */
    public void updateLeaderboard() {
        leaderboard.getChildren().clear();
        leaderboard.getChildren().add(leaderboardTitle);
        //Sorts scores
        userScores.sort((a, b) -> b.getValue().getValue().compareTo(a.getValue().getValue()));
        for (Pair<String, Pair<String, Integer>> user : userScores) {
            logger.info("User: " + user.getKey() + " Score: " + user.getValue().getValue());
            var userScore = new Text(user.getKey() + ": " + user.getValue().getValue());
            userScore.getStyleClass().add("heading");
            leaderboard.getChildren().add(userScore);
        }
    }

    /**
     * Send message on chat
     * @param message message to be sent to server
     */
    public void sendMessage(String message) {
        if (!message.isEmpty()) {
            com.send("MSG " + message);
            Multimedia.playAudio("message.wav");
        }
        sendChat.clear();
    }

    /**
     * Game over sends you to scores screen
     */
    @Override
    public void gameOver() {
        logger.info("Game over");
        timerBar.setWidth(0);
        timerBar.setHeight(0);
        timerBar.setOpacity(0);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    @Override
    void blockClicked(GameBlock gameBlock) {
        multiGame.blockClicked(gameBlock);
        //Checks if score is greater than high score, if greater, high score becomes current score
        if (multiGame.score.getValue() > getHighScore()) {
            logger.info("New highscore: "+ highScore);
            highScore.set(multiGame.score.getValue());
        }
    }

    /**
     * Method to rotate current piece
     */
    @Override
    public void rotate() {
        logger.info("Block rotated");
        Multimedia.playAudio("rotate.wav");
        multiGame.rotateCurrentPiece(multiGame.currentPiece);
        currentPieceBoard.setPiece(multiGame.currentPiece);
    }

    /**
     * Method which switches the current piece and the next piece to be displayed
     */
    @Override
    public void swapPiece() {
        Multimedia.playAudio("pling.wav");
        multiGame.swapCurrentPiece();
        currentPieceBoard.setPiece(multiGame.currentPiece);
        nextPieceBoard.setPiece(multiGame.nextPiece);
    }

    /**
     * Build challenge scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setUpGame();

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
        score.textProperty().bind(multiGame.score.asString());
        scoreBox.getChildren().addAll(scoreText, score);

        //Lives
        var livesBox = new VBox();
        livesBox.setAlignment(Pos.CENTER);
        var livesText = new Text("Lives");
        livesText.getStyleClass().add("heading");
        var lives = new Text();
        lives.getStyleClass().add("lives");
        lives.textProperty().bind(multiGame.lives.asString());
        livesBox.getChildren().addAll(livesText, lives);


        top.getChildren().addAll(livesBox, title, scoreBox);

        //Right box
        var right = new VBox();
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(0, 10, 0, -55));
        right.setSpacing(20);
        //down, left, up right
        mainPane.setRight(right);

        //Left box
        var left = new VBox();
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(5, 5, 5, 15));
        left.setSpacing(20);
        mainPane.setLeft(left);

        //Multiplier text
        var multiplierText = new Text("Multiplier");
        multiplierText.getStyleClass().add("heading");

        //Multiplier
        var multiplier = new Text();
        multiplier.getStyleClass().add("hiscore");
        multiplier.textProperty().bind(multiGame.multiplier.asString());

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

        //Level text
        var levelText = new Text("Level");
        levelText.getStyleClass().add("heading");

        //Level
        var level = new Text();
        level.getStyleClass().add("level");
        level.textProperty().bind(multiGame.level.asString());

        //Level box
        var levelBox = new VBox();
        levelBox.setAlignment(Pos.CENTER);
        levelBox.getChildren().addAll(levelText, level);

        //Chat title
        var chatTitle = new Text("Chat");
        chatTitle.getStyleClass().add("title");

        //Chat area
        var chat = new TextArea();
        chat.setEditable(false);
        chat.setPrefSize(200, 100); // Set preferred width and height

        //Text field
        sendChat = new TextField();
        sendChat.setPromptText("Send chat message");
        sendChat.setOnAction(event -> sendMessage(sendChat.getText()));

        //Chat box
        var chatBox = new VBox();
        chatBox.getChildren().addAll(chatTitle, chat, sendChat);

        //Listens for messages
        com.addListener(msg -> {
            if (msg.startsWith("MSG")) {
                chat.appendText(msg.substring(4) + "\n");
            }
        });

        //Leaderboard box
        leaderboard = new VBox();


        //Leaderboard title
        leaderboardTitle = new Text("Leaderboard");
        leaderboardTitle.getStyleClass().add("title");
        leaderboard.getChildren().add(leaderboardTitle);

        right.getChildren().addAll(levelBox, multiplierBox, currentPieceBox, nextPieceBox);
        left.getChildren().addAll(leaderboard, chatBox);

        //Made board into global variable
        board = new GameBoard(multiGame.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        board.setPadding(new Insets(0, 80, 0, 0));
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
}
