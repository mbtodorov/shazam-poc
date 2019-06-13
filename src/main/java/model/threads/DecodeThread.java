package main.java.model.threads;

import main.java.view.SongBtn;
import main.java.model.fingerprint.AudioDecoder;

public class DecodeThread extends Thread {
    private String song;
    private SongBtn btn;

    public DecodeThread(String song) {
        this.song = song;
    }

    @Override
    public void run() {
        double[][] FFTResult = AudioDecoder.decodeWav(song);
        btn.setPoints(FFTResult);
    }

    public void doRun(SongBtn btn) {
        this.btn = btn;
        start();
    }
}
