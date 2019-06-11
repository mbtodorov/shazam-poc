package client.view;

import javafx.scene.control.Button;

/**
 * A simple class used for buttons that are linked with songs
 * Used to store information about the song within the button
 * It extends Button.
 *
 * @version 1.0
 * @author Martin Todorov
 */
class SongBtn extends Button {
    // The result of an FFT applied on the song
    private double[][] points;

    /**
     * Constructor
     * @param name the song name
     * @param points the result of an FFT applied to the song
     */
    SongBtn(String name, double[][] points) {
        super(name);
        this.points = points;
    }

    /**
     * Accessor method
     * @return the result of the FFT algorithm
     */
    double[][] getPoints() {
        return points;
    }
}
