package main.java.model.engine.datastructures;

/**
 * A simple class to store a keypoint from the spectrogram.
 * It stores its time (x) and frequency (y)
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class KeyPoint {
    private int time;
    private int frequency;

    /**
     * Constructor
     *
     * @param time the x coordinate of the point
     * @param frequency the y coordinate of the point
     */
    public KeyPoint(int time, int frequency) {
        this.time = time;
        this.frequency = frequency;
    }

    /** Setters and Getters: */

    public int getTime() {
        return time;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}