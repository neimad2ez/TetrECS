package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * Pieceboard is a component which displays upcoming pieces in grids
 */
public class PieceBoard extends GameBoard{
    private static final Logger logger = LogManager.getLogger(PieceBoard.class);

    /**
     * Create a new pieceboard variable
     * @param width width of pieceboard
     * @param height height of pieceboard
     */
    public PieceBoard(double width, double height) {
        super(3, 3, width, height);
        logger.info("Create new piece board");
    }

    /**
     * Displays the piece on a certain grid
     * @param gp
     */
    public void setPiece(GamePiece gp) {
        logger.info("Sets pieces");
        grid.clear();
        //We do x and y = 1 as we account for offset. Offset subtracts 1 from both creating a negative answer if we put x and y = 0.
        grid.playPiece(gp, 1, 1);
    }
}
