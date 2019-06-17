package main.java.model.concurrent.task;

import javafx.concurrent.Task;
import main.java.model.fingerprint.AudioDecoder;

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
    private final static Logger logger = Logger.getLogger(AudioDecoder.class.getName());

    // the byte array which contains the mic extract
    private byte[] raw;
    // the microphone format used
    private AudioFormat micFormat;

    /**
     * Constructor
     *
     * @param raw the mic data
     * @param micFormat the mic format used
     */
    MicMatcher(byte[] raw, AudioFormat micFormat) {
        this.raw = raw;
        this.micFormat = micFormat;
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
        // convert raw byte array to input stream
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        AudioInputStream stream = new AudioInputStream(bais, micFormat, raw.length);

        logger.log(Level.INFO, "Begin decoding and matching microphone extract...");

        String result = AudioDecoder.decodeStreamAndMatch(stream, true);

        logger.log(Level.INFO, "Done decoding and matching microphone extract!");

        return result;
    }
}
