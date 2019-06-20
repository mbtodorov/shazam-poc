package main.java.view;

import main.java.model.engine.AudioFingerprint;
import main.java.model.engine.datastructures.KeyPoint;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
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
class Spectrogram extends VBox {
    // logger
    private final static Logger logger;
    // Strings used for the GUI
    private static final String SPECTROGRAM_LBL, KEYPTS_LBL;
    static {
        logger = Logger.getLogger(Spectrogram.class.getName());
        SPECTROGRAM_LBL = " Spectrogram for ";
        KEYPTS_LBL = "Key points extracted from the spectrogram:";
    }

    // The FFT result
    private double[][] points;
    /**
     * Constructor for class Spectrogram
     *
     * @param points the points for the spectrogram
     * @param songName the name of the song
     */
    Spectrogram(double[][] points, String songName) {
        setPrefWidth(900);

        this.points = points;

        // label for the spectrogram
        Label spectrogramLabel = new Label(SPECTROGRAM_LBL + songName.substring(0, songName.length()-4) + ": ");
        spectrogramLabel.setWrapText(true);
        spectrogramLabel.getStyleClass().add("spectrogram-label");

        // the spectrogram
        logger.log(Level.INFO, "Begin painting the spectrogram for song " + songName + "...");
        BufferedImage spectrogram = drawSpectrogram();
        javafx.scene.image.Image i = SwingFXUtils.toFXImage(spectrogram, null);
        logger.log(Level.INFO, "Successfully painted spectrogram!");

        // label for the keypoints
        Label keyPointsLabel = new Label(KEYPTS_LBL);
        keyPointsLabel.getStyleClass().add("spectrogram-label");

        // the key points
        logger.log(Level.INFO, "Begin painting the keypoints of the spectrogram...");
        BufferedImage keyPoints = drawKeyPoints();
        javafx.scene.image.Image j = SwingFXUtils.toFXImage(keyPoints, null);
        logger.log(Level.INFO, "Successfully painted keypoints!");

        // add all elements as children and align them in the center
        getChildren().addAll(spectrogramLabel, new ImageView(i), keyPointsLabel, new ImageView(j),
                new Label(""), new Label("")); // spacer labels
        setAlignment(Pos.CENTER);
    }

    /**
     * A method to draw a spectrogram based on a double[][]
     * which is the result of an FFT.
     *
     * @return an image representing the spectrogram
     */
    private BufferedImage drawSpectrogram() {
        // the spectrogram image
        BufferedImage result = new BufferedImage(points.length, points[0].length, BufferedImage.TYPE_INT_RGB);

        // we need to reverse the points because the image
        // has the y coordinate going down
        double[][] reversedPoints = new double[points.length][points[0].length];
        for(int i = 0; i < reversedPoints.length; i ++) {
            for(int j = 0; j < reversedPoints[0].length; j ++) {
                reversedPoints[i][j] = points[i][points[0].length - 1 - j];
            }
        }

        //iterate and paint based on frequency amplitude
        for (int x = 0; x < points.length; x++) {
            for (int y = 0; y < points[x].length; y++) {
                // get color based on amplitude
                Color newColor = getColor(1.0 - reversedPoints[x][y]);
                result.setRGB(x, y, newColor.getRGB());
            }
        }

        // resize to fit screen better
        result = resize(result);
        return result;
    }

    /**
     * A method to draw discernible key points from the FFT result
     * represented in the drawSpectrogram() method. It extracts the key points
     * (for a second time) from the AudioFingerprint class as they are not passed
     * as a a parameter to this class. It represents any significant points with
     * a black square around them and all other points - white. Uses buffered image
     *
     * @return the result buffered image representing the keypoints
     */
    private BufferedImage drawKeyPoints() {
        // init the result image
        BufferedImage result = new BufferedImage(points.length, points[0].length, BufferedImage.TYPE_INT_RGB);

        // color everything white
        for (int x = 0; x < points.length; x++) {
            for (int y = 0; y < points[x].length; y++) {
                Color white = Color.WHITE;
                result.setRGB(x, y, white.getRGB());
            }
        }

        // paint red lines for each logarithmic band
        Color red = Color.RED;
        for(int i = 0; i < points.length; i ++) {
            result.setRGB(i, 501, red.getRGB());
            result.setRGB(i, 502, red.getRGB());
            result.setRGB(i, 491, red.getRGB());
            result.setRGB(i, 492, red.getRGB());
            result.setRGB(i, 471, red.getRGB());
            result.setRGB(i, 472, red.getRGB());
            result.setRGB(i, 431, red.getRGB());
            result.setRGB(i, 432, red.getRGB());
            result.setRGB(i, 351, red.getRGB());
            result.setRGB(i, 352, red.getRGB());
            result.setRGB(i, 191, red.getRGB());
            result.setRGB(i, 192, red.getRGB());
        }

        // get the keypoints
        KeyPoint[][] keyPoints2D = AudioFingerprint.extractKeyPoints(points);

        // convert to single dimension array
        ArrayList<KeyPoint> keyPointsList = new ArrayList<>();
        for (KeyPoint[] value : keyPoints2D) {
            keyPointsList.addAll(Arrays.asList(value));
        }

        KeyPoint[] keyPoints = keyPointsList.toArray(new KeyPoint[0]);

        // reverse the frequencies of the points
        // because the image has the y coordinate going down
        for(KeyPoint kp : keyPoints) {
            kp.setFrequency(511 - kp.getFrequency());
        }

        for(KeyPoint kp : keyPoints) {
            int x = kp.getTime();
            int y = kp.getFrequency();
            int sqSize = 4;
            int xFloor = x - sqSize;
            if(xFloor < 0) xFloor = 0; // edge
            int xCeil = x + sqSize;
            if(xCeil > points.length) xCeil = points.length - 1; // edge
            int yFloor = y - sqSize/2; // divide by 2 because of resize
            if(yFloor < 0) yFloor = 0; // edge
            int yCeil = y + sqSize/2; // divide by 2 because of resize
            if(yCeil > points[0].length) yCeil = points[0].length - 1; // edge
            for(int i = xFloor; i < xCeil; i ++ ) { //iterate and paint square around point
                for(int j = yFloor; j < yCeil; j ++) {
                    Color black = Color.BLACK;
                    result.setRGB(i, j, black.getRGB());
                }
            }
        }

        /* -- Uncomment to only paint a pixel, not an entire square
        // paint a black point for each keypoint
        for(KeyPoint kp : keyPoints) {
            int x = kp.getTime();
            int y = kp.getFrequency();
            Color black = Color.BLACK;
            result.setRGB(x, y, black.getRGB());
        }
         */


        // resize to fit screen better
        result = resize(result);
        return result;
    }

    /**
     * A method to resize a buffered image to 800(w) by 300(h)
     *
     * @param img the image to be resized
     * @return the resized image
     */
    private BufferedImage resize(BufferedImage img) {
        int width = img.getWidth();
        if(width > 800) {
            width = 800;
        }
        Image tmp = img.getScaledInstance(width, 300, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(width, 300, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return result;
    }

    /**
     * A method to get a color based on the intensity of the
     * amplitude
     *
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
