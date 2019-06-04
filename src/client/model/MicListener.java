package client.model;

import client.model.exc.MatchFoundException;
import client.model.exc.NoMatchException;

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

    // static strings for exception descriptions
    private static final String MATCH_FOUND, NO_MATCH_FOUND;
    static {
        MATCH_FOUND = "Success! A match has been found: ";
        NO_MATCH_FOUND = "No match has been found. Try Again.";
    }

    /**
     * The main algorithm to try and match audio from mic to DB.
     * Runs for 10 seconds.
     *
     * @throws NoMatchException no match has been found for 10 seconds
     * @throws MatchFoundException a matching song has been found
     */
    public static void listen() throws NoMatchException, MatchFoundException {
        logger.log(Level.INFO, "Trying to connect to mic...");
        logger.log(Level.INFO, "Connected successfully.");
        logger.log(Level.INFO, "Listening...");

        // TODO: implement
        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException ex) {

        }
        throw new NoMatchException(NO_MATCH_FOUND);
    }
}
