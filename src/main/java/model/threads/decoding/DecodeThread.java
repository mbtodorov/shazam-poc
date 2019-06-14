package main.java.model.threads.decoding;

import main.java.view.SongBtn;
import main.java.model.fingerprint.AudioDecoder;

/**
 * This class is used instantiate the computation of each song in the
 * music directory in a new thread. It contains a reference to the SongBtn
 * so it can pass it the results from the FFT when they are done.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class DecodeThread extends Thread {
    // the name of the song that this thread will decode
    private String song;
    // the button that contains this song
    private SongBtn btn;

    /**
     * Constructor
     *
     * @param btn reference to the button of the song
     */
    public DecodeThread(SongBtn btn) {
        this.btn = btn;
        this.song = btn.getName();
    }

    /**
     * Begins the computation of the song in a new thread
     */
    @Override
    public void run() {
        // begin computation
        double[][] FFTResult = AudioDecoder.decodeWav(song);
        // pass results when done
        btn.setPoints(FFTResult);
    }
}
