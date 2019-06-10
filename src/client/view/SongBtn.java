package client.view;

import javafx.scene.control.Button;

public class SongBtn extends Button {
    private double[][] points;

    public SongBtn(String name, double[][] points) {
        super(name);
        this.points = points;
    }

    public double[][] getPoints() {
        return points;
    }
}
