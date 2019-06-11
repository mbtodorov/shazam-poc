package client.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import server.fingerprint.AudioFingerprint;

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
    // Strings used for the GUI
    private static final String SPECTROGRAM_LBL, KEYPTS_LBL;
    static {
        SPECTROGRAM_LBL = " Spectrogram for ";
        KEYPTS_LBL = "Key points extracted from the spectrogram:";
    }

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
        theImage = resize(theImage);
        return theImage;
    }

    private BufferedImage drawKeyPoints() {
        // get the keypoints
        double[][] keyPoints = AudioFingerprint.extractKeyPoints(points);
        BufferedImage theImage = new BufferedImage(keyPoints.length, keyPoints[0].length, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < keyPoints.length; x++) {
            for(int y = 0; y < keyPoints[x].length; y++) {
                // if the keypoint is not 0 paint a black square around it
                if(keyPoints[x][y] != 0) {
                    int sqSize = 4;
                    int xFloor = x - sqSize;
                    if(xFloor < 0) xFloor = 0; // edge
                    int xCeil = x + sqSize;
                    if(xCeil > keyPoints.length) xCeil = keyPoints.length - 1; // edge
                    int yFloor = y - sqSize/2;
                    if(yFloor < 0) yFloor = 0; // edge
                    int yCeil = y + sqSize/2;
                    if(yCeil > keyPoints[0].length) yCeil = keyPoints[0].length - 1; // edge
                    for(int i = xFloor; i < xCeil; i ++ ) { //iterate and paint square around point
                        for(int j = yFloor; j < yCeil; j ++) {
                            Color black = Color.BLACK;
                            theImage.setRGB(i, j, black.getRGB());
                        }
                    }
                } else { // if its not paint the pixel white
                    Color white = Color.WHITE;
                    theImage.setRGB(x, y, white.getRGB());
                }
            }
        }
        theImage = resize(theImage);
        return theImage;
    }

    /**
     * A method to resize a buffered image to 800(w) by 300(h)
     *
     * @param img the image to be resized
     * @return the resized image
     */
    private BufferedImage resize(BufferedImage img) {
        Image tmp = img.getScaledInstance(800, 300, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(800, 300, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return result;
    }

    /**
     * A method to get a color based on the intenisty of the
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
