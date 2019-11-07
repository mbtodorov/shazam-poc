package model.concurrent.task;

import model.engine.AudioDecoder;

import javafx.concurrent.Task;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used for decoding and matching audio streams
 * It takes the stream and splits and begins decoding it part
 * by part - each of which has a length equal to the EXTRACT_LENGTH
 * static field. There are edge cases where if there is only < 2 seconds
 * left from the stream it doesn't get split. Each of those parts is then
 * consecutively decoded and matched. If a match is found, it returns the song
 * name as a String, else returns no match found.
 *
 * Note: This class can be improved by rather that passing the extract
 * consecutively, it passes each one in a new thread. Which are managed by
 * a thread pool - so that if a file that has 10000 extracts of length EXTRACT_LENGTH
 * doesn't start 10000 threads.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class FileMatcher extends Task<String> {
    // statics:
    private static final Logger logger;
    private static final int EXTRACT_LENGTH;
    private static final String MATCH_FOUND, MATCH_NOT_FOUND, TOO_LONG;
    static {
        logger = Logger.getLogger(FileMatcher.class.getName());
        EXTRACT_LENGTH = 20; // the length in seconds of the split streams
        MATCH_FOUND = "This is: ";
        MATCH_NOT_FOUND = "No match found. Try again.";
        TOO_LONG = "File is too long.";
    }

    // the format of the original stream
    private AudioFormat format;
    // the entire stream
    private AudioInputStream ais;
    // the entire stream raw
    private byte[] raw;

    /**
     * Constructor
     *
     * @param ais the audio stream which is to be decoded & matched
     */
    public FileMatcher(AudioInputStream ais) {
        this.ais = ais;
        format = ais.getFormat();
        raw = null;
    }

    /**
     * The main method of the class. It begins taking parts of the
     * stream consecutively and passes them to the AudioDecoder class
     * for decoding and matching. Runs until an extract is matched to a song
     * or all extracts have returned no result.
     *
     * @return no match found string if none of the extracts were matched
     * and match found + song name if there was a match for one of the extracts.
     */
    @Override
    public String call() {
        // get the length of the stream in seconds
        float durationInSeconds = getDurationInSeconds(ais);
        // get the stream as a raw file
        try {
            raw = ais.readAllBytes();
            ais.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception while trying to convert to raw " + e);
        }

        int count = 1; // count extracts for logging

        // loop and decode & match extracts
        String result;
        for(int i = 0; i < durationInSeconds; i += EXTRACT_LENGTH) {
            logger.log(Level.INFO, "Begin fetching extract number " + count);
            AudioInputStream extract;
            // check if there will be less than two seconds remaining
            // in the original stream
            if((durationInSeconds - 2) < (i + EXTRACT_LENGTH)) {
                // if there is, get the extract until the end;
                extract = getExtract(i, true);
                i += EXTRACT_LENGTH;
            } else {
                // if there isn't - normal
                extract = getExtract(i, false);
            }

            // if extract is null then the file was too long - will be displayed on the label
            if(extract == null) return TOO_LONG;

            logger.log(Level.INFO, "Extract fetched. Duration: " + getDurationInSeconds(extract) + "s.");

            logger.log(Level.INFO, "Begin decoding & matching stream extract " + count + "...");

            result = AudioDecoder.decodeStreamAndMatch(extract, false);

            logger.log(Level.INFO, "Done decoding & matching stream extract " + count + "!");

            // if there was a match - the task is done - return
            if(result != null) return MATCH_FOUND + result;

            count++;
        }

        // getting here means that there was no match for the entire stream
        return MATCH_NOT_FOUND;
    }

    /**
     * A method to generate an extract from the original
     * AudioInputStream
     *
     * @param start the start second of the extract
     * @param untilEnd should the extract be until the end
     * @return an extract from the starting second with length equal to EXTRACT_LENGTH
     */
    private AudioInputStream getExtract(int start, boolean untilEnd) {
        // calculate the bytes per second of the stream
        float bytesPerSecond = format.getFrameSize() * format.getFrameRate();

        // init the result
        AudioInputStream extract = null;
        try {
            // calculate the start & end frames
            long start_ = (long) (start * bytesPerSecond);
            long end = (long) ((start + EXTRACT_LENGTH) * bytesPerSecond);
            if(start_ > Integer.MAX_VALUE || end > Integer.MAX_VALUE) {
                logger.log(Level.SEVERE, "Integer overflow");
                return null;
            }

            int startFrame = (int) start_;
            int endFrame = (int) end;
            if (endFrame > raw.length || untilEnd) endFrame = raw.length;

            // get raw audio only from start to end frame
            ByteArrayInputStream bais = new ByteArrayInputStream(Arrays.copyOfRange(raw, startFrame, endFrame));
            // convert to AudioInputStream
            extract = new AudioInputStream(bais, format, endFrame - startFrame);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while trying to get stream extract: " + e);
            e.printStackTrace();
        }

        return extract;
    }

    /**
     * A method to calculate the duration of an AudioInputStream
     * in seconds.
     *
     * @param stream the stream
     * @return the duration of the stream (in seconds)
     */
    private float getDurationInSeconds(AudioInputStream stream) {
        long audioFileLength = 0;
        try {
            audioFileLength = stream.available();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown while calculating duration in seconds" + e);
        }
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        return (audioFileLength / (frameSize * frameRate));
    }
}
