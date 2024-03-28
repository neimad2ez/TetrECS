package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.HashSet;

/**
 * LineClearedListener listens for when a line is filled and clears that line
 */

public interface LineClearedListener {
    /**
     * Handles the clearing of lines
     * @param coordinates Coordinates of where the lines that need to be cleared are
     */
    void lineCleared(HashSet<GameBlockCoordinate> coordinates);
}
