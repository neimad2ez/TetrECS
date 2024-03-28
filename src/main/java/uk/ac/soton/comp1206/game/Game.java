package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;
    /**
     * Current game piece
     */
    public GamePiece currentPiece = spawnPiece();
    /**
     * Next game piece which can be swapped with current piece
     */
    public GamePiece nextPiece;
    /**
     * Initialises the score variable
     */
    public IntegerProperty score = new SimpleIntegerProperty(0);
    /**
     * Initialises the level variable
     */
    public IntegerProperty level = new SimpleIntegerProperty(0);
    /**
     * Initialises the lives variable
     */
    public IntegerProperty lives = new SimpleIntegerProperty(3);
    /**
     * Initialises the multiplier variable
     */
    public IntegerProperty multiplier = new SimpleIntegerProperty(1);
    /**
     * Initialises number of blocks to be seen in Game method
     */
    private int numOfBlocks = 0;
    /**
     * Next Piece Listener
     */
    protected NextPieceListener nextPieceListener = null;
    /**
     * Line cleared listener
     */
    protected LineClearedListener lineClearedListener = null;
    /**
     * Calls gameLoop method
     */
    protected ScheduledExecutorService timer;
    protected ScheduledFuture<?> loop;
    /**
     * Game loop listener
     */
    protected GameLoopListener gameLoopListener = null;
    /**
     * Game over listener
     */
    protected GameOverListener gameOverListener;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        //Creates a single threaded scheduled executor
        timer = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if (gameLoopListener != null) {
            logger.info("Game looped");
            gameLoopListener.setOnGameLoop(getTimerDelay());
        }
    }


    public void initialiseGame() {
        logger.info("Initialising game");
        this.nextPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //Checks if you can play the piece then, places the piece and gets a new piece
        if (grid.canPlayPiece(currentPiece,x,y)) {
            grid.playPiece(currentPiece,x,y);
            Multimedia.playAudio("place.wav");
            afterPiece();
            nextPiece();
        } else {
            Multimedia.playAudio("fail.wav");
        }


//        //Get the new value for this block
//        int previousValue = grid.get(x,y);
//        int newValue = previousValue + 1;
//        if (newValue  > GamePiece.PIECES) {
//            newValue = 0;
//        }
//
//        //Update the grid with the new value
//        grid.set(x,y,newValue);
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     *Retrieves a newly created piece
     * @return returns next piece to be put onto grid
     */
    public void nextPiece() {
        logger.info("Next piece is created");
        //Current piece becomes next piece
        currentPiece = nextPiece;
        //Next piece gets another piece
        nextPiece = spawnPiece();

        logger.info("Current piece is " + currentPiece);
        logger.info("Next piece is " + nextPiece);
        //If next piece occurs, call nextPiece method on currentPiece to get the next piece from nextPiece variable
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece);
        }
    }

    /**
     * Listens for next piece
     * @param listener Next Piece Listener
     */
    public void setNextPieceListener(NextPieceListener listener) {
        nextPieceListener = listener;
    }

    /**
     * Uses random number generator to select a random piece from list of pieces.
     * @return returns random piece that has been selected from random num generator
     */

    public GamePiece spawnPiece() {
        logger.info("Selects a random piece from list of pieces");
        int maxPieces = GamePiece.PIECES;
        Random random = new Random();
        var randomPiece = random.nextInt(maxPieces);
        logger.info("Picking a random piece: {}", randomPiece);
        var piece = GamePiece.createPiece(randomPiece);
        return piece;
    }

    /**
     * Contains logic to handle the clearance of the lines
     */
    public void afterPiece() {
        logger.info("This method contains the logic handling the clearance of lines");
        int linesCleared = 0;
        //HashSet of GameBlockCoordinates which keeps track of how many lines need to be cleared
        var cleared = new HashSet<GameBlockCoordinate>();
        //HashSet of GameBlockCoordinates which keeps track of how many blocks need to be reset
        var reset = new HashSet<GameBlockCoordinate>();
        //Horizontal clearing of lines, left to right
        //Iterate through the grid
        for (int x = 0; x < rows; x++) {
            int counter = 0;
            for (int y = 0; y < cols; y++) {
                //Checks if there is a block in the coordinate x,y. If there is you increment counter
                if (grid.get(x, y) != 0) {
                    counter++;
                }
                //Checks if there are 5 blocks in a row, if counter = rows then there is a line present
                if (counter == rows) {
                    linesCleared++;
                    for (y = 0; y < rows; y++) {
                        cleared.add(new GameBlockCoordinate(x, y));
                        reset.add(new GameBlockCoordinate(x, y));
                    }
                }
            }
        }

        // 0 0 0 0 0
        // 0 0 0 0 0
        // 0 0 0 0 0
        // 0 0 0 0 0
        // 0 0 0 0 0

        //Vertical clearing of lines, up to down
        //Iterate through the grid
        for (int y = 0; y < cols; y++) {
            int counter = 0;
            for (int x = 0; x < rows; x++) {
                //Checks if there is a block in the coordinate x,y. If there is you increment counter
                if (grid.get(x,y) != 0) {
                    counter++;
                }
                //Checks if there are 5 blocks in a row, if counter = rows then there is a line present
                if (counter == rows) {
                    linesCleared++;
                    //As y = ? has a line to clear, you loop through x again and add them all to the HashSet
                    for (x = 0; x < cols; x++) {
                        cleared.add(new GameBlockCoordinate(x, y));
                        reset.add(new GameBlockCoordinate(x, y));
                    }
                }
            }
        }
        for (GameBlockCoordinate g: cleared) {
            int x = g.getX();
            int y = g.getY();
            if (grid.get(x,y) != 0) {
                numOfBlocks++;
            }
        }

        logger.info("Num of blocks = " + numOfBlocks);

        for (GameBlockCoordinate g: reset) {
            int x = g.getX();
            int y = g.getY();
            if (grid.get(x,y) != 0) {
                grid.set(x,y,0);
            }
        }
//        gameBoard.fadeOut(cleared);
        if (lineClearedListener != null) {
            lineClearedListener.lineCleared(cleared);
        }
        score(linesCleared, numOfBlocks);
        multiplier(linesCleared);
        level();
    }

    /**
     * Adds the scores depending on how many lines the user clears
     * @param numOfLines number of lines cleared
     * @param numOfBlocks number of blocks cleared
     */
    public void score(int numOfLines, int numOfBlocks) {
        logger.info("Method which implements the scoring system");
        //If number of lines cleared = 0 then there is no score to be added
        if (numOfLines == 0) {
            return;
        }
        //Gets multiplier (different as it's a simpleintegerproperty)
        int mul = multiplier.get();
        int toAdd = numOfLines * numOfBlocks * 10 * mul;
        score.setValue(toAdd);
    }

    /**
     * multiplier method which increases multiplier if line is cleared and resets multiplier back to 1 if line isn't cleared
     * @param numOfLines number of lines cleared
     */
    public void multiplier(int numOfLines) {
        //Streak is number of consecutive times player has cleared a line
        int streak = 0;
        //If at least 1 line is cleared it increases streak and increases multiplier
        if (numOfLines > 0) {
            streak++;
            multiplier.setValue(multiplier.get() + streak);
        } else {
            multiplier.setValue(1);
            streak = 0;
        }
    }

    /**
     * Retrieves the current level
     */
    public void level() {
        logger.info("Method that implements the different levels a user is on");
        level.setValue(score.get() / 1000);
    }

    /**
     * Rotates current piece to the right
     * @param gp GamePiece that needs to be rotated
     */
    public void rotateCurrentPiece(GamePiece gp) {
        currentPiece.rotate();
    }

    /**
     * Swaps the current piece with the next piece
     */

    public void swapCurrentPiece() {
        GamePiece temp = currentPiece;
        currentPiece = nextPiece;
        nextPiece = temp;
    }

    /**
     * Listens for when line is cleared
     * @param lineClearedListener LineClearedListener
     */
    public void setOnLineCleared(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Calculates delay at the maximum of 2500 ms or 12000-500*current level
     */
    public int getTimerDelay() {
        return Math.max(12000 - 500 * level.get(), 2500);
    }

    /**
     * Handle events when timer ends
     * @param gameLoopListener listens for when timer ends
     */
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * Events that happen when timer ends
     */
    public void gameLoop() {
        updateLives();
        updateMultiplier();
        nextPiece();
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Removes a life or stops game when there are no lives left
     */
    public void updateLives() {
        if (lives.get() > 0) {
            logger.info("Life lost");
            lives.set(lives.get() - 1);
            Multimedia.playAudio("lifelose.wav");
        } else {
            logger.info("Game over");
            if (gameOverListener != null) {
                logger.info("IT WORKED");
                gameOverListener.GameOver();
            }

        }
    }

    /**
     * Makes multiplier back to 1 when life lost
     */
    public void updateMultiplier() {
        logger.info("Multiplier set back to 1");
        multiplier.set(1);
    }

    public void setOnGameOver(GameOverListener gameOverListener) {
        this.gameOverListener = gameOverListener;
    }


//    public void clear(HashSet<GameBlockCoordinate> hash) {
//        for (GameBlockCoordinate g: hash) {
//            int x = g.getX();
//            int y = g.getY();
//            if (grid.get(x,y) == 1) {
//                grid.set(x,y,0);
//            }
//        }
//    }



}
