package main.java.model.concurrent.task;

import javafx.concurrent.Task;
import main.java.model.engine.AudioDecoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamMatcher extends Task<String> {
    // statics:
    private static final Logger logger;
    private static final int EXTRACT_DURATION; // in seconds
    private static final String MATCH_FOUND, MATCH_NOT_FOUND;
    static {
        logger = Logger.getLogger(StreamMatcher.class.getName());
        EXTRACT_DURATION = 5;
        MATCH_FOUND = "This is: ";
        MATCH_NOT_FOUND = "No match found. Try again.";
    }

    private AudioFormat format;
    private AudioInputStream ais;
    private byte[] raw;


    public StreamMatcher(AudioInputStream ais) {
        this.ais = ais;
        format = ais.getFormat();
        raw = null;
    }

    @Override
    public String call() {
        // get the length of the stream in seconds
        float durationInSeconds = getDurationInSeconds(ais);
        try {
            raw = ais.readAllBytes();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception while trying to convert to raw " + e);
        }
        // loop and decode extracts
        String result = null;
        for(int i = 0; i < durationInSeconds; i += EXTRACT_DURATION) {
            System.out.println("index: " + i);
            AudioInputStream extract = getExtract(i);
            result = AudioDecoder.decodeStreamAndMatch(extract, false);
            // TODO: FFT breaks if extract is too small
            if(result != null) return MATCH_FOUND + result;
        }

        return MATCH_NOT_FOUND;
    }

    private AudioInputStream getExtract(int start) {
        // extract the bytes per second of the stream
        float bytesPerSecond = format.getFrameSize() * format.getFrameRate();

        AudioInputStream extract = null;
        try {
            long start_ = (long) (start * bytesPerSecond);
            long end = (long) ((start + EXTRACT_DURATION) * bytesPerSecond);
            if(start_ > Integer.MAX_VALUE || end > Integer.MAX_VALUE) {
                logger.log(Level.SEVERE, "Integer overflow");
                return null;
            }

            int startFrame = (int) start_;
            int endFrame = (int) end;
            if (endFrame > raw.length) endFrame = raw.length;

            ByteArrayInputStream bais = new ByteArrayInputStream(Arrays.copyOfRange(raw, startFrame, endFrame));
            extract = new AudioInputStream(bais, format, endFrame - startFrame);

            getDurationInSeconds(extract);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while trying to get stream extract: " + e);
            e.printStackTrace();
        }

        return extract;
    }

    private float getDurationInSeconds(AudioInputStream stream) {
        long audioFileLength = 0;
        try {
            audioFileLength = stream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        float durationInSeconds = (audioFileLength / (frameSize * frameRate));
        System.out.println("duration: " + durationInSeconds);
        return durationInSeconds;
    }
}
