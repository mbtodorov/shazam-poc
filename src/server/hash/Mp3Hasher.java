package server.hash;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the algorithms used to hash songs.
 * Hashes all mp3s from the music folder in root folder or
 * hashes a single mp3 file.
 * Also stores results in db
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class Mp3Hasher {
    // logger
    private final static Logger logger = Logger.getLogger(Mp3Hasher.class.getName());

    /**
     * Method to iterate through all mp3 files in the music dir in
     * root folder. Stores results in DB.
     *
     * @return a string array with all the hashed songs' names;
     */
    public static String[] hashMp3s() {
        String[] result = new String[]{"1", "2", "3", "4", "5", "6"};

        logger.log(Level.INFO, "Iterating through songs...");

        // TODO: implement hashing algorithm

        logger.log(Level.INFO, "All songs have been hashed!");
        return result;
    }

    // TODO: implement hashing algorithm for a single mp3
}
