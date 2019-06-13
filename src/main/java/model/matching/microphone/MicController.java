package main.java.model.matching.microphone;

import main.java.model.exc.MatchFoundException;
import main.java.model.exc.NoMatchException;

import javax.sound.sampled.AudioFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for registering analog audio from
 * the microphone. It looks for matches in the database and throws
 * an exception which stops the algorithm and returns a match
 * (if the song playing has been identified) or an error (if no
 * match has been found).
 *
 * @version 1.0;
 * @author Martin Todorov
 */
public class MicController {
    // logger
    private final static Logger logger = Logger.getLogger(MicController.class.getName());

    private AudioFormat format;
    public MicController() {
        format = getAudioFormat();
    }

    /**
     * The main algorithm to try and match audio from mic to DB.
     * Runs for 10 seconds.
     *
     * @throws NoMatchException no match has been found for 10 seconds
     * @throws MatchFoundException a matching song has been found
     */
    public void listen() throws Exception{
        logger.log(Level.INFO, "Trying to connect to mic...");

        MicListener t1 = new MicListener(format);
        t1.start();

        try{
            t1.join();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to join threads. Terminating...");
            System.exit(-1);
        }
        t1.checkForException();
    }

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
