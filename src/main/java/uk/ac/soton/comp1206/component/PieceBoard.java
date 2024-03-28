package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

public class PieceBoard extends GameBoard{
    private static final Logger logger = LogManager.getLogger(PieceBoard.class);
    public PieceBoard(double width, double height) {
        super(3, 3, width, height);
        logger.info("Create new piece board");
    }

    public void setPiece(GamePiece gp) {
        logger.info("Sets pieces");
        grid.clear();
        //We do x and y = 1 as we account for offset. Offset subtracts 1 from both creating a negative answer if we put x and y = 0.
        grid.playPiece(gp, 1, 1);
    }
}
