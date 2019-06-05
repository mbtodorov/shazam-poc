package client.view;

import javafx.scene.control.Button;
import server.dsts.Complex;

public class SongBtn extends Button {
    private Complex[][] points;

    public SongBtn(String name, Complex[][] points) {
        super(name);
        this.points = points;
    }

    public Complex[][] getPoints() {
        return points;
    }
}
