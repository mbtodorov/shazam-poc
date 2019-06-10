package client.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class used to visualize the results from an FFT algorithm
 * on a song as well as determination of key points. It is not
 * necessary but it is useful for testing purposes.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class Spectrogram extends VBox {
    // logger
    private final static Logger logger = Logger.getLogger(Spectrogram.class.getName());

    private double[][] points;

    /**
     * Constructor for class Spectrogram
     *
     * @param points the points for the spectrogram
     * @param songName the name of the song
     */
    public Spectrogram (double[][] points, String songName) {
        setPrefWidth(900);

        this.points = points;

        // label for the spectrogram
        Label spectrogramLabel = new Label("Spectrogram for " + songName.substring(0, songName.length()-4) + ": ");
        spectrogramLabel.setWrapText(true);
        spectrogramLabel.getStyleClass().add("spectrogram-label");

        // the spectrogram
        logger.log(Level.INFO, "Begin painting the spectrogram for song " + songName + "...");
        BufferedImage spectrogram = drawSpectrogram();
        javafx.scene.image.Image i = SwingFXUtils.toFXImage(spectrogram, null);
        logger.log(Level.INFO, "Successfully painted spectrogram!");

        // label for the keypoints
        Label keyPointsLabel = new Label("Key points: ");
        keyPointsLabel.getStyleClass().add("spectrogram-label");

        // the key points
        logger.log(Level.INFO, "Begin painting the keypoints of the spectrogram...");
        // TODO: keypoints painter:
        logger.log(Level.INFO, "Successfully painted keypoints!");

        // add all elements as children and align them in the center
        getChildren().addAll(spectrogramLabel, new ImageView(i), keyPointsLabel);
        setAlignment(Pos.CENTER);
    }

    /**
     * A method to draw a spectrogram based on a double[][]
     * which is the result of an FFT.
     * @return an image represeting the spectrogram
     */
    private BufferedImage drawSpectrogram() {
        BufferedImage theImage = new BufferedImage(points.length, points[0].length, BufferedImage.TYPE_INT_RGB);
        double ratio;

        //iterate and paint based on frequency amplitude
        for (int x = 0; x < points.length; x++) {
            for (int y = 0; y < points[x].length; y++) {
                ratio = points[x][y];
                Color newColor = getColor(1.0 - ratio);
                theImage.setRGB(x, y, newColor.getRGB());
            }
        }

        // resize so it can fit in the window
        theImage = resize(theImage, 800, 300);
        return theImage;
    }

    /**
     * A method to reisze a buffered image
     * @param img the image to be resized
     * @param newW the new width
     * @param newH the new height
     * @return the resized image
     */
    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return result;
    }

    /**
     * A method to get a color based on the intenisty of the
     * amplitude
     * @param power the intensity
     * @return the color
     */
    private Color getColor(double power) {
        double H = power * 0.3; // Hue
        double S = 1.0; // Saturation
        double B = 1.0; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }
}
