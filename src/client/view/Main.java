package client.view;

import client.model.MicListener;
import server.hash.Mp3Hasher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the proof of concept application.
 * Creates and example GUI for testing purposes.
 *
 * How to use:
 * Step 1: 'Precompute' all songs (found in the music dir)
 * Step 2: 'Go' and feed analog audio to microphone of device
 * The program will listen for up to 10 seconds and match
 * the fed audio with one of the precomputed songs. If not - err
 *
 * Clicking on song will show spectrogram
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class Main extends Application {
    // logger
    private final static Logger logger = Logger.getLogger(Main.class.getName());

    // Strings used for the GUI
    private static final String WIN_TITLE, BTN_PRECOMPUTE, BTN_GO,
                                LISTENING;
    static {
        WIN_TITLE = "Shazam PoC";
        BTN_PRECOMPUTE = "Pre-compute Songs";
        BTN_GO = "Go";
        LISTENING = "Listening...";
    }

    // Displays names of all the songs found in the music dir
    private GridPane songGrid;
    // begins listening to the microphone
    private Button goBtn;
    // logs status of app
    private Label info;
    // keep stage as field for resizing purposes
    private Stage mStage;

    /**
     * Creates the example GUI and sets the stage for
     * testing purposes
     *
     * @param stage stage
     * @throws Exception possible exc
     */
    @Override
    public void start(Stage stage) throws Exception{
        logger.log(Level.INFO, "Launching application...");

        // get the stage for resizing
        mStage = stage;

        // compute button
        Button computeBtn = new Button(BTN_PRECOMPUTE);
        computeBtn.setOnAction(this::computeSongs);
        computeBtn.getStyleClass().add("compute-btn");

        // grid for all songs from music dir
        songGrid = new GridPane();
        songGrid.setAlignment(Pos.CENTER);
        songGrid.setHgap(20);
        songGrid.setVgap(5);

        // 'Go' button
        goBtn = new Button(BTN_GO);
        goBtn.setOnAction(this::go);
        toggleGoBtnDisable(); // initially disabled
        goBtn.getStyleClass().add("go-btn");

        // label to display status
        info = new Label();
        info.setWrapText(true);
        info.getStyleClass().add("info-label");

        // root pane
        VBox root = new VBox(computeBtn, songGrid, goBtn, info);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        // scene & stage:
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(WIN_TITLE);
        stage.setScene(scene);
        stage.show();
        stage.sizeToScene();
        stage.setOnCloseRequest(e -> Platform.exit());

        logger.log(Level.INFO, "Successfully launched application!");
    }

    /**
     * Main method used to launch the app
     *
     * @param args main method
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method precomputes all of the songs found in the
     * music dir of the root folder of this project. It stores them
     * in a database with hashes so they can be compared to later.
     * Shazam supposedly has it's songs computer beforehand.
     * Then display them in the GUI grid
     *
     * @param e btn
     */
    private void computeSongs(ActionEvent e) {
        logger.log(Level.INFO, "Hashing algorithm called...");

        // enable go button
        toggleGoBtnDisable();

        // hash songs into db
        String[] songs = Mp3Hasher.hashMp3s();

        logger. log(Level.INFO, "Hashing algorithm executed successfully!");
        // display in grid
        for(int i = 0; i < songs.length; i++) {
            Button songBtn = new Button(songs[i]);
            songBtn.setOnAction(this::showSpectrogram);
            songBtn.getStyleClass().add("song-btn");

            songGrid.add(songBtn, i%2, i/2);
        }

        logger.log(Level.INFO, "Hashed Songs added to grid.");

        // disable compute btn
        Button btn = (Button) e.getSource();
        btn.setDisable(true);

        // resize
        mStage.sizeToScene();
    }

    /**
     * This method begins listening to the analog audio fed in the
     * microphone of the device that this app is running on. It will
     * hash and try to find a match for a song in the precomputed db
     * If nothing has been found in 10 seconds, returns error.
     *
     * @param e btn
     */
    private void go(ActionEvent e) {
        toggleGoBtnDisable();
        info.setText(LISTENING);

        MicListener listener = new MicListener();
        try {
            logger.log(Level.INFO, "Calling mic-listening algorithm...");
            // listen to mic
            listener.listen();
        }
        catch(Exception exc) {
            // exception will be thrown if a match was found or not
            // alert user with results
            info.setText(exc.getMessage());
            logger.log(Level.INFO, "Listening ended.");
        }
        finally {
            //re-enable button
            toggleGoBtnDisable();
        }
    }

    /**
     * Toggle method for enabling/disabling
     * the go button. Example: disabled while listening, or
     * before songs have been precomputed, as there would be nothing to
     * compare to.
     */
    private void toggleGoBtnDisable() {
        goBtn.setDisable(!goBtn.isDisabled());
    }

    /**
     * Method to display a spectrogram of a hashed song from the
     * grid pane.
     *
     * @param e btn
     */
    private void showSpectrogram(ActionEvent e) {
        // get the song
        Button btn = (Button) e.getSource();
        String song = btn.getText();

        logger.log(Level.INFO, "Begin painting the spectrogram for song " + song + "...");

        //TODO: implement new window with spectrogram

        logger.log(Level.INFO, "Successfully painted spectrogram!");
    }
}
