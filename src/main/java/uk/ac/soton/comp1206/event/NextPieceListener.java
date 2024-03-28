package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Next Piece Listener listens for when new piece is generated
 */
public interface NextPieceListener {
    /**
     * Deals with next piece
     * @param gp GamePiece to add to game
     */
    void nextPiece(GamePiece gp);
}
