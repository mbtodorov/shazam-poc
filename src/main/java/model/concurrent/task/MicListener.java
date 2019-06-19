package main.java.model.concurrent.task;

import javafx.concurrent.Task;
import main.java.model.engine.AudioDecoder;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for connecting the microphone
 * It is a Task which returns a String when it is done. The string
 * will be the result of the decoding and matching of the microphone.
 * It will either be a match found + song name or no match found.
 * The matching is done by taking every X seconds of microphone input
 * and opening a new task to decode and match them with the database.
 * This runs for Y amount of time and if there are no matches, returns.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class MicListener extends Task<String> {
    // statics
    private final static Logger logger ;
    private final static int LISTENING_DURATION, EXTRACT_LENGTH;
    private static final String MATCH_FOUND, MATCH_NOT_FOUND, LINE_NOT_SUPPORTED;
    static {
        logger = Logger.getLogger(AudioDecoder.class.getName());
        LISTENING_DURATION = 5; // How many times it will get X seconds of mic input
        EXTRACT_LENGTH = 5000; // The mic input duration in seconds
        MATCH_FOUND = "This is: ";
        MATCH_NOT_FOUND = "No match found. Try again.";
        LINE_NOT_SUPPORTED = "Line not supported.";
    }

    // indicates if there was a matched song
    // the value will be null if not
    private String matchedSong;
    // variable used to stop the loop if there is a match already.
    private boolean running = true;

    /**
     * The main method of the class. It opens a connection to the microphone
     * and extracts every X seconds of it in the form of a byte array. Then
     * each of the extracts gets passed for processing in a new thread.
     *
     * TODO: implement thread pool
     *
     * @return a string which would indicate the result of the matching. It will either
     * be a match found + song or no match found.
     */
    @Override
    public String call() {
        try {
            logger.log(Level.INFO, "Trying to connect to mic!");
            // get the format (11025 Hz 16 bit mono)
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // check if line is supported
            if(!AudioSystem.isLineSupported(info)) {
                return LINE_NOT_SUPPORTED;
            }

            // init target data line
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open();

            logger.log(Level.INFO, "Connected! Listening...");

            ExecutorService executor = Executors.newFixedThreadPool(LISTENING_DURATION);
            // loop for LISTENING_DURATION seconds and extract every
            // EXTRACT_LENGTH second(s) for decoding and matching
            int i = 0;
            while(running && i < LISTENING_DURATION) {
                // start receiving input
                line.start();

                // variables for storing the input
                ByteArrayOutputStream out  = new ByteArrayOutputStream();
                int numBytesRead;
                byte[] data = new byte[1024];

                line.start();

                // get the input for EXTRACT_LENGTH second(s)
                long end = System.currentTimeMillis() + EXTRACT_LENGTH;
                while (System.currentTimeMillis() < end) {
                    // Read the next chunk of data from the TargetDataLine.
                    numBytesRead =  line.read(data, 0, data.length);
                    // Save this chunk of data.
                    out.write(data, 0, numBytesRead);
                }

                // start a new task to decode and match the bytes from the input
                MicMatcher micMatcher = new MicMatcher(out.toByteArray(), format, i*EXTRACT_LENGTH/1000, EXTRACT_LENGTH);
                // on succeeding if there is a match found, the loop will break
                // check the setMatchedSong method for reference
                micMatcher.setOnSucceeded(e -> setMatchedSong(micMatcher.getValue()));

                executor.submit(new Thread(micMatcher));

                // stop
                line.stop();
                i ++;
            }

            // close the line
            line.close();

            if(running) {
                System.out.print("Awaiting termination");
                while(!executor.isTerminated()) {
                    Thread.sleep(10);
                }
            }

            logger.log(Level.INFO, "Listening ended.");

            // check if there was a match
            if(matchedSong != null) {
                executor.shutdownNow();
                return MATCH_FOUND + matchedSong;
            }
        }
        catch (Exception e) {
            return e.getMessage(); // the exception will be displayed in the label
        }

        // if we get to here there was no match found
        return MATCH_NOT_FOUND;
    }

    /**
     * A method to indicate that that there was a match found
     * while decoding, by assigning a value to a private variable.
     * It gets called every time a task succeeded.
     *
     * @param song the result of the task
     */
    private void setMatchedSong(String song) {
        // the task will return null if there was no match found
        if(song != null) {
            matchedSong = song;
            // stop the loop
            running = false;
        }
    }

    /**
     * A method to define the audio format that the mic recording
     * will be in. It has a low sampling rate so we dont have to
     * down sample it later.
     *
     * @return the format of the mic recording
     */
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
