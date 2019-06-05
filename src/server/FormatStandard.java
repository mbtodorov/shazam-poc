package server;

import javax.sound.sampled.AudioFormat;

public class FormatStandard {

    public static AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 8;
        int channels = 1; // mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels,
                signed, bigEndian);
    }
}
