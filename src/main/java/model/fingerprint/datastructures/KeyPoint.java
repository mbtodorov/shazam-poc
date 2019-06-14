package main.java.model.fingerprint.datastructures;

/**
 * A simple class to store data from a 2D spectrogram
 * which contains only key points from a song.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class KeyPoint {
    //fields
    private int time;
    private int frequency;

    /**
     * Constructor
     *
     * @param time the time of the point
     * @param frequency the frequency of the point
     */
    public KeyPoint(int time, int frequency) {
        this.time = time;
        this.frequency = frequency;
    }

    /**
     * Accessor
     */
    public int getTime() {
        return time;
    }

    /**
     * Accessor
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Modifier
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * Modifier
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}