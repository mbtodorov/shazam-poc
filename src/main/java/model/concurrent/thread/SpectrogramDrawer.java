package main.java.model.concurrent.thread;

import javafx.embed.swing.SwingFXUtils;
import main.java.model.engine.AudioDecoder;
import main.java.model.engine.AudioFingerprint;
import main.java.model.engine.datastructures.KeyPoint;
import main.java.view.audio.Spectrogram;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

/** A thread class used to draw spectrograms without loading the
 * JavaFX thread. It runs the same computation on the song except it
 * stops before hashing and uses the FFT result double[][]
 *
 * @author Martin Todorov
 * @version 1.0
 */
public class SpectrogramDrawer extends Thread{

    // where the thread was called from
    private Spectrogram spectrogramPane;
    // the song
    private String song;

    /**
     * Constructor
     *
     * @param spectrogramPane the GUI window
     * @param song the song for which the spectrogram is drawn
     */
    public SpectrogramDrawer(Spectrogram spectrogramPane, String song) {
        this.song = song;
        this.spectrogramPane = spectrogramPane;
    }

    /**
     * When starting the thread, get the FFT results and then use them to first
     * draw a spectrogram and then a 2D graph of extracted key points.
     */
    @Override
    public void run() {
        // run computation
        double[][] points = AudioDecoder.decodeWav(song, true);

        // the spectrogram
        BufferedImage spectrogram = drawSpectrogram(points);
        javafx.scene.image.Image theSpectrogram = SwingFXUtils.toFXImage(spectrogram, null);
        // pass image to GUI
        spectrogramPane.setSpectrogram(theSpectrogram);

        // the key points
        BufferedImage keyPoints = drawKeyPoints(points);
        javafx.scene.image.Image theKeyPoints = SwingFXUtils.toFXImage(keyPoints, null);
        // pass image to GUI
        spectrogramPane.setKeyPoints(theKeyPoints);
    }

    /**
     * A method to draw a spectrogram based on a double[][]
     * which is the result of an FFT.
     *
     * @return an image representing the spectrogram
     */
    private BufferedImage drawSpectrogram(double[][] points) {
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
    private BufferedImage drawKeyPoints(double[][] points) {
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
