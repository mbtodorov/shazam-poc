package main.java.view;

import main.java.model.concurrent.thread.DecodeThread;
import main.java.model.concurrent.task.MicListener;
import main.java.model.concurrent.task.StreamMatcher;
import main.java.model.db.DBUtils;
import main.java.model.engine.AudioDecoder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;

// material design controls
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the proof of concept application.
 * Creates and example GUI for testing purposes.
 *
 * The GUI contains detailed instructions on the usage of the
 * application.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class Main extends Application {
    // logger
    private final static Logger logger;
    // Strings used for the GUI
    private static final String WIN_TITLE, BTN_COMPUTE, BTN_GO,
                                LISTENING, COMPUTATION_DONE, NO_NEW_SONGS,
                                NEW_SONGS, USE_INPUT_STREAM, CHOOSE_FILE,
                                ALERT_ERROR, ALERT_NO_FILE, ALERT_UNSUPPORTED,
                                COMPARING;
    static {
        logger = Logger.getLogger(Main.class.getName());
        WIN_TITLE = "Shazam PoC";
        BTN_COMPUTE = "Compute";
        BTN_GO = "Go";
        LISTENING = "Listening...";
        NO_NEW_SONGS = "All songs in the music dir are recognizable. \n 'Compute' will generate their spectrograms only.";
        NEW_SONGS = " song(s) in the music dir which are not recognizable. \n 'Compute' will generate their " +
                "fingerprints. ";
        COMPUTATION_DONE = "The following songs are recognizable:";
        USE_INPUT_STREAM = "Use input stream instead of mic.";
        CHOOSE_FILE = "Choose File";
        ALERT_ERROR = "Error!";
        ALERT_NO_FILE = "No file was selected. Try again.";
        ALERT_UNSUPPORTED = "The selected file's audio format is not supported. Supported format:";
        COMPARING = "Looking for a match...";
    }

    // the songs in the music dir
    private String[] songs;

    // shows info for the user
    private Label infoLbl;
    // Displays names of all the songs found in the music dir
    private VBox songGrid;
    // toggle button to switch between mic and input stream
    private ToggleButton useInputStream;
    // begins listening to the microphone
    private Button goBtn;
    // prompts the user the select input file to match
    private Button chooseFile;
    // logs match status of input
    private Label matchLbl;

    // keep the root so children can be added/removed
    private VBox root;
    // keep stage as field for resizing purposes
    private Stage stage;

    /**
     * Creates the example GUI and sets the stage for
     * testing purposes.
     *
     * @param stage stage
     * @throws Exception possible exc
     */
    @SuppressWarnings("RedundantThrows")
    @Override
    public void start(Stage stage) throws Exception{
        logger.log(Level.INFO, "Launching application...");

        // check database connection
        if(DBUtils.checkConnection()) {
            // make sure there are songs in the music folder
            this.songs = AudioDecoder.scanForSongs();
            if (songs.length == 0) {
                logger.log(Level.SEVERE, "No songs found in music dir. Exiting...");
                System.exit(-1);
            }

            // check if the schema exists and create it if not
            if (!DBUtils.existsDB()) DBUtils.initDB();

            // get the stage for resizing
            this.stage = stage;

            // check if there are any songs which haven't been hashed
            int newSongs = DBUtils.checkForNewSongs(songs);

            // notify the user accordingly with a varying label text
            if (newSongs != 0) {
                infoLbl = new Label("There are " + newSongs + NEW_SONGS);
            } else {
                infoLbl = new Label(NO_NEW_SONGS);
            }

            infoLbl.setWrapText(true);
            infoLbl.getStyleClass().add("info-label");
            infoLbl.setTextAlignment(TextAlignment.CENTER);

            // compute btn
            Button computeBtn = new JFXButton(BTN_COMPUTE);
            computeBtn.setOnAction(this::computeSongs);
            computeBtn.getStyleClass().add("btn");

            // grid for displaying all songs from music dir
            songGrid = new VBox();
            songGrid.setAlignment(Pos.CENTER);
            songGrid.getStyleClass().add("song-grid");

            // toggle between input stream and microphone
            useInputStream = new JFXToggleButton();
            useInputStream.setText(USE_INPUT_STREAM);
            useInputStream.setOnAction(this::toggleInput);
            useInputStream.getStyleClass().add("toggle-btn");

            // 'Go' button - for mic
            goBtn = new JFXButton(BTN_GO);
            goBtn.setOnAction(this::go);
            goBtn.getStyleClass().add("go-btn");

            // 'Choose File' button - for input stream
            chooseFile = new JFXButton(CHOOSE_FILE);
            chooseFile.setOnAction(this::chooseFile);
            chooseFile.getStyleClass().add("btn");

            if (newSongs != 0) enableMatching(false); // disable if there are new songs

            // label to display match status
            matchLbl = new Label();
            matchLbl.setWrapText(true);
            matchLbl.getStyleClass().add("match-label");

            // root pane
            root = new VBox();
            root.getChildren().addAll(infoLbl, computeBtn, useInputStream, goBtn);
            root.setAlignment(Pos.CENTER);
            root.getStyleClass().add("root-pane");

            // scene & stage:
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());

            stage.setTitle(WIN_TITLE);
            stage.setScene(scene);
            stage.show();
            stage.sizeToScene();
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("./style/icon.png")));
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

        // remove the button - not needed anymore
        Button btn = (Button) e.getSource();
        root.getChildren().remove(btn);

        // add the grid to contain songs as the 2nd child of the root
        root.getChildren().add(1, songGrid);

        // iterate through songs
        for (String song : songs) {
            logger.log(Level.INFO, "Decoding song " + song);

            populateGrid(song); // calls computation on the song
        }

        // update label
        infoLbl.setText(COMPUTATION_DONE);

        // enable matching if it was disabled
        enableMatching(true);

        // resize
        stage.sizeToScene();
    }

    /**
     * A method to add a song (.wav) to be displayed in the
     * songGrid GUI element. Each song is represented with a SongBtn class,
     * which stores the result of the FFT on the raw audio file and uses it to
     * draw a spectrogram when clicked. Computation of each song will be started in a
     * new thread. More information can be found in the thread class. The grid can not
     * display > 5 songs.
     *
     * @param song the song to be decoded
     */
    private void populateGrid(String song) {

        // init the btn for the song
        SongBtn songBtn = new SongBtn(song);

        // start the computation in a new thread
        // a reference is passed so the FFT result can be assigned when computation is done
        DecodeThread decodeThread = new DecodeThread(songBtn);
        decodeThread.start();

        // action handler & other
        songBtn.setOnAction(this::showSpectrogram);
        songBtn.getStyleClass().add("song-btn");
        songBtn.setWrapText(true);

        // add btn to grid
        if(songGrid.getChildren().size() < 6) {
            songGrid.getChildren().add(songBtn);
            if(songGrid.getChildren().size() == 5) {
                songGrid.getChildren().add(new Label("..."));
            }
        }
    }

    /**
     * This is the method used to connect to the microphone and
     * try to find a match in the DB for the input. Creates a new Task
     * which does the work. Displays result on a label.
     *
     * @param e btn
     */
    @SuppressWarnings("unused")
    private void go(ActionEvent e) {
        // disable matching
        enableMatching(false);

        // add the match label if it isn't added already
        addMatchLabel();

        try {
            // update label
            matchLbl.setText(LISTENING);

            // create the task
            MicListener micListener = new MicListener();
            // when its done listening - update label and re-enable matching
            micListener.setOnSucceeded(event -> {
                enableMatching(true);
                matchLbl.setText(micListener.getValue());
            });

            // begin
            new Thread(micListener).start();
        }
        catch(Exception exc) {
            logger.log(Level.SEVERE, "Exception thrown while listening: " + exc)  ;
        }
    }

    /**
     * This method is invoked from the choose file button. It prompts
     * the user to select an audio file of appropriate format. Once
     * an acceptable file is selected a new Task is started which tries to
     * find a match in the DB. Displays result on a label
     *
     * @param e the choose file button
     */
    private void chooseFile(ActionEvent e) {
        // file chooser for wav files only
        FileChooser.ExtensionFilter wavFilter = new FileChooser.ExtensionFilter("WAV Files", "*.wav");
        FileChooser wavChooser = new FileChooser();
        wavChooser.getExtensionFilters().add(wavFilter);
        File input = wavChooser.showOpenDialog(stage);

        // input is null if cancel was clicked
        if(input != null) {
            // check if the format is supported
            boolean isFormatSupported = AudioDecoder.checkFormat(input);
            if(isFormatSupported) {
                // add a match status label & disable matching until done
                addMatchLabel();
                enableMatching(false);
                matchLbl.setText(COMPARING);

                try {
                    // create the task and start it, passing the input file
                    StreamMatcher streamMatcher = new StreamMatcher(AudioSystem.getAudioInputStream(input));
                    // when done, enable matching and show result on status label
                    streamMatcher.setOnSucceeded(event -> {
                        enableMatching(true);
                        matchLbl.setText(streamMatcher.getValue());
                    });

                    // begin
                    new Thread(streamMatcher).start();
                } catch (Exception xcc) {
                    logger.log(Level.SEVERE, "Exception thrown while matching stream " + e);
                }
            } else { // audio file of unsupported format - alert
                AudioFormat format = AudioDecoder.getSupportedFormat();
                Alert alertUnsupported = new Alert(Alert.AlertType.ERROR);
                alertUnsupported.setTitle(ALERT_ERROR);
                alertUnsupported.setHeaderText(null);
                alertUnsupported.setContentText(ALERT_UNSUPPORTED + format.toString());
                alertUnsupported.show();
            }
        } else { // cancel was click - alert
            Alert alertNoFile = new Alert(Alert.AlertType.ERROR);
            alertNoFile.setTitle(ALERT_ERROR);
            alertNoFile.setHeaderText(null);
            alertNoFile.setContentText(ALERT_NO_FILE);
            alertNoFile.show();
        }
    }

    /**
     * Method for enabling/disabling match choices.
     * They can, for example be disabled in the DB is empty or when
     * one of them is running already.
     *
     * @param enable true stands for enabled buttons and false for disabled
     */
    private void enableMatching(boolean enable) {
        useInputStream.setDisable(!enable);
        goBtn.setDisable(!enable);
        chooseFile.setDisable(!enable);
    }

    /**
     * A method to toggle between microphone input and audio stream
     * input.
     *
     * @param e the toggle button
     */
    private void toggleInput(ActionEvent e) {
        // get the selected property of the toggle button
        JFXToggleButton btn = (JFXToggleButton) e.getSource();
        boolean isSelected = btn.selectedProperty().get();

        root.getChildren().remove(matchLbl);

        if(isSelected) {
            root.getChildren().remove(goBtn);
            root.getChildren().add(chooseFile);
        } else {
            root.getChildren().remove(chooseFile);
            root.getChildren().add(goBtn);
        }

        logger.log(Level.INFO, "Switched input method.");

        // resize
        stage.sizeToScene();
    }

    /**
     * Method to display a spectrogram of a song from song grid.
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
        newScene.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());

        Stage newStage = new Stage();
        newStage.setScene(newScene);
        newStage.setTitle(song);
        newStage.getIcons().add(new Image(Main.class.getResourceAsStream("./style/icon.png")));
        newStage.show();
    }

    /**
     * A method to add the match status label if it isn't added already.
     */
    private void addMatchLabel() {
        if(!root.getChildren().get(root.getChildren().size() - 1).equals(matchLbl)) {
            root.getChildren().add(matchLbl);
        }

        stage.sizeToScene();
    }
}
