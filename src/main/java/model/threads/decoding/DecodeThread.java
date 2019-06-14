package main.java.model.threads.decoding;

import main.java.view.SongBtn;
import main.java.model.fingerprint.AudioDecoder;

public class DecodeThread extends Thread {
    private String song;
    private SongBtn btn;

    public DecodeThread(SongBtn btn) {
        this.btn = btn;
        this.song = btn.getName();
    }

    @Override
    public void run() {
        double[][] FFTResult = AudioDecoder.decodeWav(song);
        btn.setPoints(FFTResult);
    }
}
