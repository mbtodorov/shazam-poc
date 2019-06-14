package main.java.model.threads.matching;

import main.java.model.fingerprint.AudioDecoder;

import javax.sound.sampled.AudioInputStream;

public class MicMatcher extends Thread {

    private AudioInputStream ais;

    @Override
    public void run () {
        AudioDecoder.decodeMic(ais);
    }

    public void doRun(AudioInputStream ais) {
        this.ais = ais;
        start();
    }
}
