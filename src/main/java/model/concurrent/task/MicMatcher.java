package model.concurrent.task;

import model.engine.AudioDecoder;

import javafx.concurrent.Task;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Task class is responsible for decoding and matching
 * an extract from the microphone input with the existing database
 * in a new thread. It will return a String - null if there was no
 * match found for the input or the name of the matched song.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class MicMatcher extends Task<String> {
    // logger
    private final static Logger logger = Logger.getLogger(MicMatcher.class.getName());

    // the byte array which contains the mic extract
    private byte[] raw;
    // the microphone format used
    private AudioFormat micFormat;
    // the beginning of the extract (second) - for logging
    private int beginning;
    // the length of the extract (seconds) - for logging
    private int extractLength;

    /**
     * Constructor
     *
     * @param raw the mic data
     * @param micFormat the mic format used
     * @param beginning = the beginning of the extract (second)
     * @param extractLength = the length of the extract (seconds)
     */
    MicMatcher(byte[] raw, AudioFormat micFormat, int beginning, int extractLength) {
        this.raw = raw;
        this.micFormat = micFormat;
        this.beginning = beginning;
        this.extractLength = extractLength/1000;
    }

    /**
     * This is the main method of the class.
     * It converts the raw audio data to an input stream
     * and passes it along for decoding & matching.
     *
     * @return null if there was no match found or the name of the
     * song which was matched
     */
    @Override
    public String call() {
        // convert raw byte array to audio input stream
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        AudioInputStream stream = new AudioInputStream(bais, micFormat, raw.length);

        logger.log(Level.INFO, "Begin decoding and matching microphone extract from " +
                beginning + "s to " + (beginning + extractLength) + "s...");

        String result = AudioDecoder.decodeStreamAndMatch(stream, true);

        logger.log(Level.INFO, "Done decoding and matching microphone extract from " +
                beginning + "s to " + (beginning + extractLength) + "s!");

        return result;
    }
}
