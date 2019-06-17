package main.java.model.concurrent.task;

import javafx.concurrent.Task;
import main.java.model.fingerprint.AudioDecoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamMatcher extends Task<String> {
    // statics:
    private static final Logger logger;
    private static final int EXTRACT_DURATION; // in seconds
    private static final String MATCH_FOUND, MATCH_NOT_FOUND;
    static {
        logger = Logger.getLogger(StreamMatcher.class.getName());
        EXTRACT_DURATION = 2;
        MATCH_FOUND = "This is: ";
        MATCH_NOT_FOUND = "No match found. Try again.";
    }

    private AudioFormat format;
    private AudioInputStream ais;

    public StreamMatcher(AudioInputStream ais) {
        this.ais = ais;
        format = ais.getFormat();
    }

    @Override
    public String call() {
        /*
        // get the length of the stream in seconds
        long audioFileLength = 0;
        try {
            audioFileLength = ais.available();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown: " + e);
        }
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        float durationInSeconds = (audioFileLength / (frameSize * frameRate));

        String result = null;
        for(int i = 0; i < durationInSeconds; i += EXTRACT_DURATION) {
            System.out.println("index: " + i);
            AudioInputStream extract = getExtract(i);
            result = AudioDecoder.decodeStreamAndMatch(extract, false);
            if(result != null) return MATCH_FOUND + result;
        }
         */
        String result = AudioDecoder.decodeStreamAndMatch(ais, false);
        if(result != null) return MATCH_FOUND + result;
        return MATCH_NOT_FOUND;
    }

    private AudioInputStream getExtract(int start) {
        // extract the bytes per second of the stream
        float bytesPerSecond = format.getFrameSize() * format.getFrameRate();

        AudioInputStream extract = null;
        AudioInputStream result = null;
        try {
            // init the result
            extract = AudioSystem.getAudioInputStream(format, ais);

            System.out.println("extract available beginning: " + extract.available());
            // get the starting byte - start (second) * bytes per second

            long startFrame = (long) (start * bytesPerSecond);
            System.out.println("startFrame: " + startFrame + "");
            synchronized (extract) {
                long skipped = extract.skip(startFrame * 2);

                System.out.println("skipped bytes: " + skipped);
            }
            long endFrame = (long) ((start + EXTRACT_DURATION) * bytesPerSecond);
            System.out.println("end frame: " + endFrame + " available in extract: " + extract.available() );
            if (endFrame > ais.available()) endFrame = ais.available();


            result = new AudioInputStream(extract, format, endFrame - startFrame);

            long audioFileLength = result.available();
            System.out.println(result.available() + " " + (endFrame -startFrame));
            int frameSize = ais.getFormat().getFrameSize();
            float frameRate = ais.getFormat().getFrameRate();
            float durationInSeconds = (audioFileLength / (frameSize * frameRate));
            System.out.println(durationInSeconds);



            extract.close(); // ?????????
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while trying to get stream extract: " + e);
        }


        return result;
    }
}
