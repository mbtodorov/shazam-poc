package main.java.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class which sends insert and query statements to the database
 * All of the methods are static and it is thread-safe.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class DBModifier {
    // logger
    private static final Logger logger = Logger.getLogger(DBUtils.class.getName());

    /**
     * A method which inserts an entry in the SONGS table in the database
     * with a give song's name.
     *
     * @param song the TITLE of the new entry in the SONGS TABLE
     */
    public static void initSongInDB(String song) {
        try {
            // connect to database
            Class.forName(DBConnection.DRIVER);
            Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            // create a statement
            Statement st = connection.createStatement();

            // insert the song in the database
            st.executeUpdate("LOCK TABLES SONGS WRITE;");
            st.executeUpdate("INSERT INTO SONGS (TITLE) VALUES ('" + song + "');");
            st.executeUpdate("UNLOCK TABLES");
            logger.log(Level.INFO, "Inserted song in database: " + song);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while trying to insert song " + song + ": \n" + e.toString());
        }
    }

    public static void insertHashes(String[] hashes, String songName) {
        try {
            // connect to database
            Class.forName(DBConnection.DRIVER);
            Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            // create a statement
            Statement st = connection.createStatement();

            // get the ID of the song
            ResultSet set = st.executeQuery("SELECT ID_SONG FROM SONGS WHERE TITLE = '" + songName + "'");
            int id = 0;
            while(set.next()) {
                id = set.getInt(1);
            }

            // insert hashes
            logger.log(Level.INFO, "Inserting hashes for song " + songName + " (id: " + id + ") in DB...");
            st.executeUpdate("LOCK TABLES HASHES WRITE;");
            for(String hash : hashes) {
                st.executeUpdate("INSERT INTO HASHES (HASH_, SONG_ID) VALUES ('" + hash + "'," + id +");");
            }
            st.executeUpdate("UNLOCK TABLES");
            logger.log(Level.INFO, "Done inserting hashes for song " + songName + " (id: " + id + ") in DB!");

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
