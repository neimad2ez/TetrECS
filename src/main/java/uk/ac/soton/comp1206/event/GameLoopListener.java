package uk.ac.soton.comp1206.event;

/**
 * Listens for when Game loops
 */
public interface GameLoopListener {
    /**
     * What happens when countdown ends
     */
    public void setOnGameLoop(int delay);
}
