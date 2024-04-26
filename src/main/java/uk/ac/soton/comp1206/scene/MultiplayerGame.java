package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Multiplayer game class that extends the game class
 */
public class MultiplayerGame extends Game {
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    /**
     * Communicator variable
     */
    private Communicator com;

    private final Queue<GamePiece> pieceQueue = new LinkedList<>();
    private int count = 0;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(Communicator com, int cols, int rows) {
        super(cols, rows);
        this.com = com;
        com.send("PIECE");
        requestPieces();
    }

    /**
     * Start the game
     */
    @Override
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if (gameLoopListener != null) {
            logger.info("Game looped");
            gameLoopListener.setOnGameLoop(getTimerDelay());
        }
    }

    /**
     * Initialise a new game and set up anything needed to start the game
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        this.nextPiece = pieceQueue.poll();
        nextPiece();
        com.send("PIECE");
        logger.info("Current piece: " + currentPiece);
        logger.info("Next piece: " + nextPiece);
    }

    /**
     * Sets a timer task to handle the pieces
     */
    public void requestPieces() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handlePiece();
            }
        }, 0, 50);
    }

    public void handlePiece() {
        if (count < 1) {
            count++;
            com.addListener(message -> {
                if (message.startsWith("PIECE")) {
                    String[] parts = message.split(" ");
                    int pieceIndex = Integer.valueOf(parts[1]);
                    logger.info("Received piece index: " + pieceIndex);
                    Platform.runLater(() -> {
                        spawnPiece(pieceIndex);
                    });
                }
            });
        }
    }

    /**
     * Creates the piece and adds it to queue
     * @param index index of the piece
     * @return return the piece just created
     */
    public GamePiece spawnPiece(int index) {
        var piece = GamePiece.createPiece(index);
        logger.info("BEFORE: " + pieceQueue.size());
        pieceQueue.add(piece);
        logger.info("AFTER: " + pieceQueue.size());
        return piece;
    }

    /**
     * Adds the scores depending on how many lines the user clears
     * @param numOfLines number of lines cleared
     * @param numOfBlocks number of blocks cleared
     */
    @Override
    protected void score(int numOfLines, int numOfBlocks) {
        super.score(numOfLines, numOfBlocks);
        com.send("SCORE " + score.get());
    }

    /**
     * Removes a life or stops game when there are no lives left
     */
    @Override
    protected void updateLives() {
        if (lives.get() > 0) {
            logger.info("Life lost");
            lives.set(lives.get() - 1);
            com.send("LIVES " + lives.get());
            Multimedia.playAudio("lifelose.wav");
        } else {
            logger.info("Game over");
            if (gameOverListener != null) {
                Platform.runLater(() -> gameOverListener.gameOver());
            }
            com.send("DIE");
            Multimedia.stopMusic();
            shutdown();
        }
    }

    /**
     *Changes current piece to the next piece and reassigns nextpiece a new piece
     */
    public void nextPiece() {
        logger.info("Next piece is created");
        //Current piece becomes next piece
        currentPiece = nextPiece;
        //Next piece gets another piece
        logger.info("BEFORE1: " + pieceQueue.size());
        nextPiece = pieceQueue.poll();
        logger.info("AFTER1: " + pieceQueue.size());
        com.send("PIECE");

        logger.info("Current piece is " + currentPiece);
        logger.info("Next piece is " + nextPiece);
        //If next piece occurs, call nextPiece method on currentPiece to get the next piece from nextPiece variable
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece);
        }
    }

}
