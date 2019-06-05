package client.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import server.dsts.Complex;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Spectrogram extends Pane {
    private final static Logger logger = Logger.getLogger(Spectrogram.class.getName());
    private final Canvas canvas;
    private Complex[][] points;

    public Spectrogram (Complex[][] points) {
        setPrefHeight(400);
        setPrefWidth(800);

        this.points = points;

        canvas = new Canvas(getWidth(), getHeight());
        getChildren().add(canvas);
        widthProperty().addListener(e -> canvas.setWidth(getWidth()));
        heightProperty().addListener(e -> canvas.setHeight(getHeight()));
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        logger.log(Level.INFO, "Began painting...");
        GraphicsContext g2d = canvas.getGraphicsContext2D();

        int blockSizeX = 2;
        int blockSizeY = 4;
        int size = 100;

        for (int i = 0; i < points.length; i++) {
            int freq = 1;
            for (int line = 1; line < size; line++) {
                // To get the magnitude of the sound at a given frequency slice
                // get the abs() from the complex number.
                // In this case I use Math.log to get a more managable number
                // (used for color)
                double magnitude = Math.log(points[i][freq].abs() + 1);
                // The more blue in the color the more intensity for a given
                // frequency point:
                g2d.setFill(Color.rgb(0, (int) magnitude * 10,
                        (int) magnitude * 20));

//				if (freq < 300 /* && recordPoints[i][freq] == 1 */) {
//					g2d.setColor(Color.RED);
//				}

                // Fill:
                g2d.fillRect(i * blockSizeX, (size - line) * blockSizeY,
                        blockSizeX, blockSizeY);

                // I used a improviced logarithmic scale and normal scale:
                if (/* logModeEnabled */false && (Math.log10(line) * Math
                        .log10(line)) > 1) {
                    freq += (int) (Math.log10(line) * Math.log10(line));
                } else {
                    freq++;
                }
            }
        }

        logger.log(Level.INFO, "Done painting!");
    }
}
