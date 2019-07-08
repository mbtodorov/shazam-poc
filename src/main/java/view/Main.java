package main.java.view;

import main.java.model.concurrent.task.MicListener;
import main.java.model.concurrent.task.FileMatcher;
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
import main.java.view.audio.SongCatalogue;

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
    private static final String WIN_TITLE,  BTN_GO,
                                LISTENING, USE_INPUT_STREAM, CHOOSE_FILE,
                                ALERT_ERROR, ALERT_NO_FILE, ALERT_UNSUPPORTED,
                                COMPARING, INFO_LBL;
    static {
        logger = Logger.getLogger(Main.class.getName());
        WIN_TITLE = "Shazam PoC";
        BTN_GO = "Go";
        LISTENING = "Listening...";
        USE_INPUT_STREAM = "Use microphone instead.";
        CHOOSE_FILE = "Choose File";
        ALERT_ERROR = "Error!";
        ALERT_NO_FILE = "No file was selected. Try again.";
        ALERT_UNSUPPORTED = "The selected file's audio format is not supported. Supported format:";
        COMPARING = "Looking for a match...";
        INFO_LBL = "Song Catalogue: ";
    }

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
     */
    @Override
    public void start(Stage stage) {
        logger.log(Level.INFO, "Launching application...");

        // check database connection
        if(DBUtils.checkConnection()) {

            // get the stage for resizing
            this.stage = stage;

            // check if the schema exists and create it if not
            if (!DBUtils.existsDB()) DBUtils.initDB();

            // title label
            Label infoLbl = new Label(INFO_LBL);
            infoLbl.setWrapText(true);
            infoLbl.getStyleClass().add("info-label");
            infoLbl.setTextAlignment(TextAlignment.CENTER);

            // the song catalogue
            SongCatalogue songCatalogue = new SongCatalogue();

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

            // label to display match status
            matchLbl = new Label();
            matchLbl.setWrapText(true);
            matchLbl.getStyleClass().add("match-label");

            // root pane
            root = new VBox();
            root.getChildren().addAll(infoLbl, songCatalogue, useInputStream, chooseFile);
            root.setAlignment(Pos.CENTER);
            root.getStyleClass().add("root-pane");

            // scene & stage:
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());

            stage.setTitle(WIN_TITLE);
            stage.setScene(scene);
            stage.show();
            stage.setMinWidth(600);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("./img/icon.ico")));
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
    @SuppressWarnings("unused")
    private void chooseFile(ActionEvent e) {
        // file chooser for wav files only
        FileChooser.ExtensionFilter wavFilter = new FileChooser.ExtensionFilter("WAV Files", "*.wav");
        FileChooser wavChooser = new FileChooser();
        wavChooser.setInitialDirectory(new File("music/teststreams"));
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
                    FileMatcher fileMatcher = new FileMatcher(AudioSystem.getAudioInputStream(input));
                    // when done, enable matching and show result on status label
                    fileMatcher.setOnSucceeded(event -> {
                        enableMatching(true);
                        matchLbl.setText(fileMatcher.getValue());
                    });

                    // begin
                    new Thread(fileMatcher).start();
                } catch (Exception exc) {
                    logger.log(Level.SEVERE, "Exception thrown while matching stream " + exc);
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

        if(!isSelected) {
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
     * A method to add the match status label if it isn't added already.
     */
    private void addMatchLabel() {
        if(!root.getChildren().get(root.getChildren().size() - 1).equals(matchLbl)) {
            root.getChildren().add(matchLbl);
        }

        stage.sizeToScene();
    }
}
