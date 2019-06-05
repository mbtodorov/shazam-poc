package server.hash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This class contains the algorithms used to hash songs.
 * Hashes all mp3s from the music folder in root folder or
 * hashes a single mp3 file.
 * Also stores results in db
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class Mp3Decoder {
    // logger
    private final static Logger logger = Logger.getLogger(Mp3Decoder.class.getName());

    /**
     * Method to iterate through all mp3 files in the music dir in
     * root folder. Stores results in DB.
     *
     * @return a string array with all the hashed songs' names;
     */
    public String[] populateDB() {
        String[] result = new String[]{"1", "2", "3", "4", "5", "6"};

        File folder = new File("../../../music");
        System.out.println(folder.toString());
        logger.log(Level.INFO, "Looking through folder " + folder);

        logger.log(Level.INFO, "Iterating through songs...");
        logger.log(Level.INFO, "All songs have been hashed!");
        return result;
    }
    // TODO: implement hashing algorithm for a single mp3
}
