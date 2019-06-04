package client.view;

import client.model.MicListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private final static Logger logger = Logger.getLogger(Main.class.getName());

    private static final String WIN_TITLE, BTN_PRECOMPUTE, BTN_GO,
                                LISTENING;
    static {
        WIN_TITLE = "Shazam PoC";
        BTN_PRECOMPUTE = "Pre-compute Songs";
        BTN_GO = "Go";
        LISTENING = "Listening...";
    }

    private Parent songGrid;
    private Button goBtn;
    private Label info;

    @Override
    public void start(Stage stage) throws Exception{
        logger.log(Level.INFO, "Launching application...");

        Button computeBtn = new Button(BTN_PRECOMPUTE);
        computeBtn.setOnAction(this::computeSongs);
        computeBtn.getStyleClass().add("compute-btn");

        songGrid = new GridPane();

        goBtn = new Button(BTN_GO);
        goBtn.setOnAction(this::go);
        toggleGoBtnDisable();
        goBtn.getStyleClass().add("go-btn");

        info = new Label();
        info.setWrapText(true);
        info.getStyleClass().add("info-label");

        VBox root = new VBox(computeBtn, songGrid, goBtn, info);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(WIN_TITLE);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> Platform.exit());

        logger.log(Level.INFO, "Successfully launched application!");
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void computeSongs(ActionEvent e) {
        toggleGoBtnDisable();
    }

    private void go(ActionEvent e) {
        toggleGoBtnDisable();
        info.setText(LISTENING);
        try {
            MicListener.listen();
        }
        catch(Exception exc) {
            info.setText(exc.getMessage());
        }
        finally {
            toggleGoBtnDisable();
        }
    }

    private void toggleGoBtnDisable() {
        goBtn.setDisable(!goBtn.isDisabled());
    }
}
