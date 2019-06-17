package main.java.model.concurrent.thread;

import main.java.view.SongBtn;
import main.java.model.fingerprint.AudioDecoder;

/**
 * This class is used to run computation of each song from the
 * music dir on a separate thread. It contains a reference to
 * the song button corresponding to the song, so it can pass it
 * the results of the FFT when done
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class DecodeThread extends Thread {
    // fields:
    private String song;
    private SongBtn btn;

    /**
     * Constructor
     *
     * @param btn the btn of the song that will be computed
     */
    public DecodeThread(SongBtn btn) {
        this.btn = btn;
        this.song = btn.getName();
    }

    /**
     * Constructor
     *
     * @param song the name of the song that will be computed
     */
    public DecodeThread(String song) {
        this.song = song;
        btn = null;
    }

    /**
     * Begins the computation of the song on a new thread.
     * Assigns results to the song button when done.
     */
    @Override
    public void run() {
        if(btn != null) {
            double[][] FFTResult = AudioDecoder.decodeWav(song);
            btn.setPoints(FFTResult);
        }
    }
}
