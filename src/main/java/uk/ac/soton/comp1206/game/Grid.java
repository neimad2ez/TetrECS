package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.IllegalFormatCodePointException;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * Logger variable to log what is currently happening
     */
    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
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
     * Checks if you can play a piece on the grid
     * @param gp Gamepiece
     * @param xCoord x-coordinate of where the piece will be played
     * @param yCoord y-coordinate of where the piece will be played
     * @return true or false to see if you can play the piece
     */
    public Boolean canPlayPiece(GamePiece gp, int xCoord, int yCoord) {
        logger.info("Checks if you can play a certain piece, returns true or false");
        //Removes offset, usually the 3x3 piece doesn't place in the middle, removing 1 allows piece to be put in the middle
        var topX = xCoord - 1;
        var topY = yCoord - 1;

        //Gets the 3x3 grid
        int[][] grid = gp.getBlocks();
        //Runs through the 3x3 grid
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {
                //Checks value at point x,y in the 3x3 grid (see if it's a 1 or 0)
                var blockValue = grid[x][y];
                //If blockValue is 1, it means there is a block in that x,y position
                if (blockValue > 0) {
                    //Finds where the blocks will be on the 5x5 grid and checks if there is already a block there
                    var gridValue = get(topX + x, topY + y);
                    //If there is already a block at the location in the 5x5, return false as blockValue is also 1
                    if (gridValue != 0) {
                        logger.info("Unable to place piece, conflict");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Play a piece by updating the grid with the piece blocks
     * @param gp the piece to play
     * @param xCoord x coordinate of where block will be placed
     * @param yCoord y coordinate of where block will be placed
     */
    public void playPiece(GamePiece gp, int xCoord, int yCoord) {
        logger.info("Checking if we can play the piece at x and y");
        //Removes offset, usually the 3x3 piece doesn't place in the middle, removing 1 allows piece to be put in the middle
        var topX = xCoord - 1;
        var topY = yCoord - 1;
        //Gets the value (which piece) it is
        int value = gp.getValue();
        //Gets the 3x3 grid of the piece
        int[][] blocks = gp.getBlocks();
        //Checks if you can play the piece
        if (!canPlayPiece(gp, xCoord, yCoord)) return;
        //Run through the 3x3 grid
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks.length; y++) {
                //Checks value wherever we are in the nested for loop
                var blockValue = blocks[x][y];
                //Checks if there is a block at the x and y coordinate of the 3x3
                if (blockValue > 0) {
                    //Put the block on the grid
                    set(x + topX, y + topY, value);
                }
            }
        }
    }

    /**
     * All lines are cleared
     */
    public void clear() {
        logger.info("Lines cleared are all set back to 0");
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                grid[x][y].set(0);
            }
        }
    }

}
