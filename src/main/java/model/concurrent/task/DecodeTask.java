package main.java.model.concurrent.task;

import javafx.concurrent.Task;
import main.java.model.engine.AudioDecoder;

/**
 * A task class which is used to invoke the main computation method
 * for decoding a song and populating the DB with its fingerprints
 * to make it recognizable.
 */
public class DecodeTask extends Task<Void> {
    // the name of the song
    private String song;
    // whether it is in the DB already (draw spectrogram only)
    private boolean isInDB;

    /**
     * Constructor
     *
     * @param song the name of the song
     * @param isInDB if false, draw spectrogram only
     */
    public DecodeTask (String song, boolean isInDB)  {
        this.song = song;
        this.isInDB = isInDB;
    }

    /**
     * Calls the decodeWav controller method which is the main
     * engine method for decoding songs
     *
     * @return nothing - its a void method
     */
    @Override
    protected Void call() {
        AudioDecoder.decodeWav(song, isInDB);
        return null;
    }
}
