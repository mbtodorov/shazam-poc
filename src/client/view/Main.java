package client.view;

import client.model.MicController;
import server.fingerprint.AudioController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
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
    private VBox songGrid;
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
        songGrid = new VBox();
        songGrid.setAlignment(Pos.CENTER);
        songGrid.setSpacing(10);

        // 'Go' button
        goBtn = new Button(BTN_GO);
        goBtn.setOnAction(this::go);
        goBtn.setDisable(true);
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
        AudioController fingerprinter = new AudioController();
        String[] songs = fingerprinter.scanForSongs();


        logger.log(Level.INFO, "Iterating through songs (only .wav) found... (" + songs.length + " total)");

        for(String song : songs) {
            logger.log(Level.INFO, "Decoding song " + song);

            SongBtn songBtn = new SongBtn(song, fingerprinter.decode(new File("music/" + song)));
            songBtn.setOnAction(this::showSpectrogram);
            songBtn.getStyleClass().add("song-btn");
            songBtn.setWrapText(true);

            songGrid.getChildren().add(songBtn);

            logger.log(Level.INFO, "Done decoding " + song);
        }

        logger.log(Level.INFO, "Decoded Songs added to grid.");

        // disable compute btn
        Button btn = (Button) e.getSource();
        btn.setDisable(true);

        // enable go button
        toggleGoBtnDisable();

        // resize
        mStage.sizeToScene();
    }

    /**
     * This method begins listening to the analog audio fed in the
     * microphone of the device that this app is running on. It will
     * fingerprint and try to find a match for a song in the precomputed db
     * If nothing has been found in 10 seconds, returns error.
     *
     * @param e btn
     */
    private void go(ActionEvent e) {
        toggleGoBtnDisable();

        MicController listener = new MicController();
        try {
            logger.log(Level.INFO, "Calling mic-listening algorithm...");
            // listen to mic
            listener.listen();
        }
        catch(Exception exc) {
            // exception will be thrown if a match was found or not
            // alert user with results
            info.setText(exc.getMessage());
            logger.log(Level.INFO, "Listening ended.")  ;
        }
        finally {
            //re-enable button
            toggleGoBtnDisable();
        }

        mStage.sizeToScene();
    }

    /**
     * Toggle method for enabling/disabling
     * the go button. Example: disabled while listening, or
     * before songs have been precomputed, as there would be nothing to
     * compare to.
     */
    private void toggleGoBtnDisable() {
        goBtn.setDisable(!goBtn.isDisabled());
        if(goBtn.isDisabled()) {
            goBtn.setText(LISTENING);
        } else {
            goBtn.setText(BTN_GO);
        }
    }

    /**
     * Method to display a spectrogram of a hashed song from the
     * grid pane.
     *
     * @param e btn
     */
    private void showSpectrogram(ActionEvent e) {
        // get the song
        SongBtn btn = (SongBtn) e.getSource();
        String song = btn.getText();

        Scene newScene = new Scene(new Spectrogram(btn.getPoints(), song));
        newScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        Stage newStage = new Stage();
        newStage.setScene(newScene);
        newStage.setTitle(song);
        newStage.show();
    }
}
