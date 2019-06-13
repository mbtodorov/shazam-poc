package main.java.view;

import main.java.model.threads.DecodeThread;
import main.java.model.matching.microphone.MicController;
import main.java.model.db.DBController;
import main.java.model.fingerprint.AudioDecoder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the proof of concept application.
 * Creates and example GUI for testing purposes.
 *
 * The GUI contains detailed instructions on the usage of the
 * application
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class Main extends Application {
    // logger
    private final static Logger logger = Logger.getLogger(Main.class.getName());

    // Strings used for the GUI
    private static final String WIN_TITLE, BTN_COMPUTE, BTN_GO,
                                LISTENING, COMPUTATION_DONE, NO_NEW_SONGS,
                                NEW_SONGS;
    static {
        WIN_TITLE = "Shazam PoC";
        BTN_COMPUTE = "Compute";
        BTN_GO = "Go";
        LISTENING = "Listening...";
        NO_NEW_SONGS = "All of the songs found in the music dir are already hashed in the database and " +
                "can be recognized. Clicking the 'compute' button will compute their spectrogram only.";
        NEW_SONGS = "There are songs in the music dir which are not hashed in the database and can't be recognized. " +
                "Clicking the 'compute' button will generate a spectrogram for all songs and hash those which are new. " +
                "Total number of new songs found: ";
        COMPUTATION_DONE = "The following songs are recognizable:";
    }

    // explains the state of the DB to the user
    private Label infoLbl;
    // Displays names of all the songs found in the music dir
    private VBox songGrid;
    // begins listening to the microphone
    private Button goBtn;
    // logs match status of input
    private Label matchLbl;
    // keep stage as field for resizing purposes
    private Stage mStage;
    // the songs in the music dir
    private String[] songs;
    // keep the root so children can be removed
    private VBox root;

    /**
     * Creates the example GUI and sets the stage for
     * testing purposes
     *
     * @param stage stage
     * @throws Exception possible exc
     */
    @SuppressWarnings("RedundantThrows")
    @Override
    public void start(Stage stage) throws Exception{
        logger.log(Level.INFO, "Launching application...");

        // check database connection
        if(DBController.checkConnection()) {
            // make sure there are songs in the music folder
            this.songs = AudioDecoder.scanForSongs();
            if (songs.length == 0) {
                logger.log(Level.SEVERE, "No songs found in music dir. Exiting...");
                Platform.exit();
                return;
            }

            // check if the schema exists and create it if not
            if (!DBController.existsDB()) DBController.initDB();

            // get the stage for resizing
            mStage = stage;

            // check if there are any songs which haven't been hashed
            int newSongs = DBController.checkForNewSongs(songs);

            // notify the user accordingly with a varying label text
            if (newSongs != 0) {
                infoLbl = new Label(NEW_SONGS + newSongs);
            } else {
                infoLbl = new Label(NO_NEW_SONGS);
            }

            infoLbl.setWrapText(true);
            infoLbl.getStyleClass().add("info-label");
            infoLbl.setTextAlignment(TextAlignment.CENTER);

            // compute btn
            Button computeBtn = new Button(BTN_COMPUTE);
            computeBtn.setOnAction(this::computeSongs);
            computeBtn.getStyleClass().add("compute-btn");

            // grid for displaying all songs from music dir
            songGrid = new VBox();
            songGrid.setAlignment(Pos.CENTER);
            songGrid.setSpacing(10);

            // 'Go' button
            goBtn = new Button(BTN_GO);
            goBtn.setOnAction(this::go);
            if (newSongs != 0) goBtn.setDisable(true); // disable if there are new songs
            goBtn.getStyleClass().add("go-btn");

            // label to display match status
            matchLbl = new Label();
            matchLbl.setWrapText(true);
            matchLbl.getStyleClass().add("match-label");

            // root pane
            root = new VBox();
            root.getChildren().addAll(infoLbl, computeBtn, songGrid, goBtn);
            root.setAlignment(Pos.CENTER);
            root.getStyleClass().add("root-pane");

            // scene & stage:
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());

            stage.setTitle(WIN_TITLE);
            stage.setScene(scene);
            stage.show();
            stage.sizeToScene();
            stage.setOnCloseRequest(e -> Platform.exit());

            logger.log(Level.INFO, "Successfully launched application!");
        }
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
     * This method iterated through all the song found in the music dir
     * and calls the decodeWav() method on them. The chained method will cross reference
     * the database and handle everything else. If the song is new, a fingerprint will be extracted.
     * If not, the computation will only get the FFTResults required for illustrating a spectrogram.
     * More information can be found in GUI labels and comments in the AudioDecoder class.
     *
     * @param e btn
     */
    private void computeSongs(ActionEvent e) {
        logger.log(Level.INFO, "Iterating through songs (only .wav) found... (" + songs.length + " total)");

        for (String song : songs) {
            logger.log(Level.INFO, "Decoding song " + song);

            populateGrid(song);
        }

        // remove the button - not needed anymore
        Button btn = (Button) e.getSource();
        root.getChildren().remove(btn);

        // update label
        infoLbl.setText(COMPUTATION_DONE);

        // enable goBtn if it was disabled
        goBtn.setDisable(false);

        // resize
        mStage.sizeToScene();
    }

    /**
     * A method to add a song (.wav) to be displayed in the
     * songGrid GUI element. Each song is represented with a SongBtn class,
     * which stores the result of the FFT on the raw audio file and uses it to
     * draw a spectrogram when clicked. Computation of each song will be started in a
     * new thread. More information can be found in the thread class.
     *
     * @param song the song to be decoded
     */
    private void populateGrid(String song) {
        // init the btn for the song
        SongBtn songBtn = new SongBtn(song);

        // start the computation in a new thread
        // a reference is passed so the FFT result can be assigned when computation is done
        DecodeThread decodeThread = new DecodeThread(song);
        decodeThread.doRun(songBtn);

        // action handler & other
        songBtn.setOnAction(this::showSpectrogram);
        songBtn.getStyleClass().add("song-btn");
        songBtn.setWrapText(true);

        // add btn to grid
        songGrid.getChildren().add(songBtn);
    }

    /**
     * This method begins listening to the analog audio fed in the
     * microphone of the device that this app is running on. It will
     * fingerprint and try to find a match for a song in the precomputed db
     * If nothing has been found in 10 seconds, returns error.
     *
     * @param e btn
     */
    @SuppressWarnings("unused")
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
            matchLbl.setText(exc.getMessage());
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
     * Method to display a spectrogram of a hashed song from song grid.
     * Also displays key points.
     *
     * @param e btn
     */
    private void showSpectrogram(ActionEvent e) {
        // get the song
        SongBtn btn = (SongBtn) e.getSource();
        String song = btn.getText();

        // create the spectrogram
        Scene newScene = new Scene(new Spectrogram(btn.getPoints(), song));
        newScene.getStylesheets().add(getClass().getResource("style/estyle.css").toExternalForm());

        Stage newStage = new Stage();
        newStage.setScene(newScene);
        newStage.setTitle(song);
        newStage.show();
    }
}
