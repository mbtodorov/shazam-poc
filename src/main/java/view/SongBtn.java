package main.java.view;

import javafx.application.Platform;
import javafx.scene.control.Button;

/**
 * A simple class used for buttons that are linked with songs
 * Used to store the result from applying FFT on the song.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class SongBtn extends Button {
    // The result of an FFT applied on the song
    private double[][] points;
    // the name
    private String name;

    /**
     * Constructor
     * @param name the song name
     */
    public SongBtn(String name) {
        super(name);
        points = null;
        this.name = name;

        // the button is disabled, because there can be a computation delay between
        // the initialization of the class and the execution of the FFT (handled in a new thread)
        setDisable(true);
        setText(getText() + " (Loading...)");
    }

    /**
     * Accessor method
     *
     * @return the result of the FFT algorithm
     */
    double[][] getPoints() {
        return points;
    }

    /**
     * Assign FFT result value as a private field
     *
     * @param points
     */
    public void setPoints(double[][] points) {
        this.points = points;

        // now that there is a value assigned, the button can be pressed to generate
        // a spectrogram using the double[][]
        setDisable(false);
        updateText();
    }

    public void updateText() {
        Platform.runLater(() -> setText(name));
    }
}