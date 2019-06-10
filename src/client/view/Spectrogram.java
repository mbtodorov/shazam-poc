package client.view;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class Spectrogram extends VBox {
    private final static Logger logger = Logger.getLogger(Spectrogram.class.getName());

    private double[][] points;

    public Spectrogram (double[][] points) {
        setPrefWidth(900);

        this.points = points;

        Label spectrogramLabel = new Label("Spectrogram: ");

        BufferedImage spectrogram = drawSpectrogram(points);
        javafx.scene.image.Image i = SwingFXUtils.toFXImage(spectrogram, null);

        getChildren().addAll(spectrogramLabel, new ImageView(i));
        setAlignment(Pos.CENTER);
        setSpacing(20);
    }

    private BufferedImage drawSpectrogram(double[][] points) {
        BufferedImage theImage = new BufferedImage(points.length, points[0].length, BufferedImage.TYPE_INT_RGB);
        double ratio;
        for (int x = 0; x < points.length; x++) {
            for (int y = 0; y < points[x].length; y++) {
                ratio = points[x][y];
                Color newColor = getColor(1.0 - ratio);
                theImage.setRGB(x, y, newColor.getRGB());
            }
        }
        theImage = resize(theImage, 800, 300);
        return theImage;
    }

    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return result;
    }

    private Color getColor(double power) {
        double H = power * 0.3; // Hue (note 0.4 = Green, see huge chart below)
        double S = 1.0; // Saturation
        double B = 1.0; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }

}
