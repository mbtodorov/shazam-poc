package main.java.view.audio;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import main.java.model.concurrent.thread.SpectrogramDrawer;

/**
 * A class used to visualize the results from an FFT algorithm
 * on a song as well as determination of key points. It is not
 * necessary but it is useful for testing purposes.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class Spectrogram extends VBox {
    // Strings used for the GUI
    private static final String SPECTROGRAM_LBL, KEYPTS_LBL;
    static {
        SPECTROGRAM_LBL = " Spectrogram for ";
        KEYPTS_LBL = "Key points extracted from the spectrogram:";
    }

    private Label loadingLabel1, loadingLabel2;

    /**
     * Constructor for class Spectrogram
     *
     * @param songName the name of the song
     */
     Spectrogram(String songName) {

        // label for the spectrogram
        Label spectrogramLabel = new Label(SPECTROGRAM_LBL + songName.substring(0, songName.length()-4) + ": ");
        spectrogramLabel.setWrapText(true);
        spectrogramLabel.getStyleClass().add("spectrogram-label");

        // label for the keypoints
        Label keyPointsLabel = new Label(KEYPTS_LBL);
        keyPointsLabel.getStyleClass().add("spectrogram-label");

        // loading labels
        javafx.scene.image.Image loading = new javafx.scene.image.Image(getClass().getResourceAsStream("./../img/loading.gif"), 50, 50, true, true);
        loadingLabel1 = new Label();
        loadingLabel2 = new Label();
        loadingLabel1.setGraphic(new ImageView(loading));
        loadingLabel2.setGraphic(new ImageView(loading));

        // add all labels to GUI
        getChildren().addAll(spectrogramLabel, loadingLabel1, keyPointsLabel, loadingLabel2);

        // begin computing the images
        SpectrogramDrawer drawThread = new SpectrogramDrawer(this, songName);
        drawThread.start();

        // alignment
        setAlignment(Pos.CENTER);

        getStyleClass().add("spectrograms-wrap");


    }

    /**
     * A method which gets called when the drawing thread computes the
     * FFT results and passes the img to be displayed in the GUI
     * @param spectrogram the spectrogram image
     */
    public void setSpectrogram(Image spectrogram) {
        Platform.runLater(() -> {
            getChildren().remove(loadingLabel1);
            getChildren().add(1, new ImageView(spectrogram));
            getScene().getWindow().sizeToScene();
            getScene().getWindow().centerOnScreen();
        });

    }

    /**
     * A method which gets called when the drawing thread computes the key points
     * and passes the image to be displayed on the GUI
     * @param keyPoints the key points image
     */
    public void setKeyPoints(Image keyPoints) {
        Platform.runLater(() -> {
            getChildren().remove(loadingLabel2);
            getChildren().add(3, new ImageView(keyPoints));
            getScene().getWindow().sizeToScene();
            getScene().getWindow().centerOnScreen();
        });
    }
}
