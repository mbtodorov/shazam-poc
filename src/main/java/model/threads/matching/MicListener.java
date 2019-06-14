package main.java.model.threads.matching;

import main.java.model.fingerprint.AudioDecoder;

import javax.sound.sampled.*;
import java.util.logging.Logger;

public class MicListener extends Thread{
    // logger
    private final static Logger logger = Logger.getLogger(AudioDecoder.class.getName());

    @Override
    public void run() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);

            final TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);

            int time = 1000;
            for(int i = 0; i < 5; i ++) {
                Thread.sleep(time);
                if(i == 0) new MicMatcher().doRun(new AudioInputStream(targetLine));
                targetLine.stop();
                targetLine.close();
            }

        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public void doRun() {

    }

    private AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        int sampleRate = 11025;
        int sampleSizeInBits = 16;
        int channels = 1;
        int frameSize = 2;
        int frameRate = 11025;
        return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize,
                frameRate, false);
    }
}
