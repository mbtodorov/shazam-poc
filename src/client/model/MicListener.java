package client.model;

import client.model.exc.MatchFoundException;
import client.model.exc.NoMatchException;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
public class MicListener {
    // logger
    private final static Logger logger = Logger.getLogger(MicListener.class.getName());

    private AudioFormat format;
    public MicListener() {
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

        MicThread t1 = new MicThread(format);
        t1.start();
        try{
            t1.join();
        }
        catch(Exception e) {

        }
        t1.checkForException();
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 8;
        int channels = 1; // mono
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels,
                               signed, bigEndian);
    }
}
