package main.java.view.audio;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.java.model.concurrent.task.DecodeTask;
import main.java.model.engine.datastructures.Song;

/**
 * A grid class which displays the song name and the
 * status of the song. The song name is a clickable button
 * if the song is in the music dir. When clicked, the spectrogram
 * will be visualized. The status of the song can be - recognizable
 * (hashed in the DB); fingerprint (on click begins hashing in the DB)
 * and loading (currently computing and hashing).
 *
 * @version 1.0
 * @author Martin Todorov
 */
class SongWrap extends GridPane {
    private static final String RECOGNIZABLE, LOADING, FINGERPRINT;
    static {
        RECOGNIZABLE = "Recognizable";
        LOADING = "Loading";
        FINGERPRINT = "Fingerprint";
    }

    // the status of the song
    private JFXButton songStatusBtn;
    // the name of the song
    private Song song;

    /**
     * Constructor
     *
     * @param song the name of the song
     */
     SongWrap(Song song) {
        this.song = song;
        // add column dimensions
        getColumnConstraints().add(new ColumnConstraints(300));
        getColumnConstraints().add(new ColumnConstraints(150));

        setHgap(10);

        // display only the song name on the button (trim the .wav)
        String buttonText = song.getName().substring(0, song.getName().length()-4);
        // the song btn
        JFXButton songName = new JFXButton(buttonText);
        songName.setTooltip(new Tooltip(buttonText));
        songName.setOnAction(this::showSpectrogram);
        songName.getStyleClass().add("song-btn");
        // disable the button if the song is not in the music dir,
        // because in order to show the spectrogram we need to apply FFT to the raw file
        if(!song.isInDir()) {
            songName.setDisable(true);
        }

        // the status btn
        songStatusBtn = new JFXButton();
        songStatusBtn.setOnAction(this::compute);
        songStatusBtn.getStyleClass().add("song-status-btn");
        updateStatus();

        // add the two buttons to the grid
        add(songName, 0, 0);
        add(songStatusBtn, 1, 0);

        // alignment
        setHalignment(songName, HPos.CENTER);
        setHalignment(songStatusBtn, HPos.CENTER);
    }

    /**
     * A method which gets called from the status button.
     * @param e the status button
     */
    @SuppressWarnings("unused")
    private void compute(ActionEvent e) {
        compute(); // begin computation
    }

    /**
     * A method which begins the computation of the song in a new thread.
     */
    private void compute() {
        // indicate that it is loading
        setLoading();
        // init a new task
        DecodeTask decodeTask = new DecodeTask(song.getName(), song.isInDB());
        decodeTask.setOnSucceeded(e -> {
            song.setInDB(); // when done, indicate that the song is in the DB
            updateStatus(); // update the status btn
        });
        // start the task in a new thread
        new Thread(decodeTask).start();
    }

    /**
     * A method used to draw the spectrogram of a song in a new window.
     *
     * @param e the song btn
     */
    @SuppressWarnings("unused")
    private void showSpectrogram(ActionEvent e){
        String songName = song.getName();

        // create the spectrogram
        Scene newScene = new Scene(new Spectrogram(songName));
        newScene.getStylesheets().add(getClass().getResource("./../style/style.css").toExternalForm());

        Stage newStage = new Stage();
        newStage.setScene(newScene);
        newStage.setTitle(songName);
        newStage.getIcons().add(new Image(SongWrap.class.getResourceAsStream("./../img/icon.ico")));
        newStage.show();
    }

    /**
     * A method to indicate that the song is being computed
     */
    private void setLoading() {
        // set text as loading and add a spinner gif
        songStatusBtn.setText(LOADING);
        Image loading = new Image(getClass().getResourceAsStream("./../img/loading.gif"), 30, 30, true, true);
        songStatusBtn.setGraphic(new ImageView(loading));
        songStatusBtn.setDisable(true);
        songStatusBtn.getStyleClass().add("loading-btn");
    }

    /**
     * A method to update the status of the song
     */
    private void updateStatus() {
        songStatusBtn.getStyleClass().remove("loading-btn");
        if(song.isInDB()) {
            songStatusBtn.setText(RECOGNIZABLE);
            Image tick = new Image(getClass().getResourceAsStream("./../img/tick.png"));
            songStatusBtn.setGraphic(new ImageView(tick));
            songStatusBtn.setDisable(true);
            songStatusBtn.getStyleClass().add("recognizable-status-btn");
        } else {
            songStatusBtn.setText(FINGERPRINT);
            songStatusBtn.setDisable(false);
            Image fingerprint = new Image(getClass().getResourceAsStream("./../img/fingerprint.png"));
            songStatusBtn.setGraphic(new ImageView(fingerprint));
        }
    }
}
