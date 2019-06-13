package main.java.model.db;

import javafx.application.Platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A controller class used to communicate with the Java app
 * and the database. It has only static methods and is therefore
 * thread safe. More can be found in the comments of the methods.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class DBController {
    // logger
    private static final Logger logger = Logger.getLogger(DBController.class.getName());

    // database connection data - change for personal database
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/shazampoc";
    private static final String USER = "root";
    private static final String PASS = "TestDBS123#@!";

    /**
     * A simple method to check if the parameters for authentication work.
     * Tries to connect to the database using the static fields above.
     *
     * @return true if the connection was successful and false if it wasn't
     */
    public static boolean checkConnection() {
        try {
            logger.log(Level.INFO, "Checking connection to database...");
            Class.forName(DRIVER);
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASS);
        } catch (Exception e) {
            // throwing an exception would mean unsuccessful connection
            logger.log(Level.SEVERE, "Unable to connect to database! Exiting...");
            Platform.exit();
            return false;
        }
        logger.log(Level.INFO, "Connection OK!");
        return true;
    }

    /**
     * A method to create the database schema. This method will get executed
     * if it is the first time running the app or if it has been dropped for some reason.
     */
    public static void initDB() {
        logger.log(Level.INFO, "No schema found in database. Creating schema...");

        try {
            // connect to the database
            Class.forName(DRIVER);
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASS);

            // create statement
            Statement statement = connection.createStatement();

            // create the schema
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS=0;");
            statement.executeUpdate("DROP TABLE IF EXISTS SONGS, HASHES;");
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS=1;");
            statement.executeUpdate("CREATE TABLE SONGS (ID_SONG INT(11) NOT NULL AUTO_INCREMENT,TITLE VARCHAR(60) NOT NULL, PRIMARY KEY (ID_SONG));");
            statement.executeUpdate("CREATE TABLE HASHES (HASH_ VARCHAR(30) NOT NULL, SONG_ID INT(11) NOT NULL);");
            statement.executeUpdate("ALTER TABLE HASHES ADD CONSTRAINT VALID FOREIGN KEY (SONG_ID) REFERENCES SONGS (ID_SONG) ON DELETE CASCADE ON UPDATE CASCADE; ");

            statement.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while creating schema: \n" + e.toString());
        }

        logger.log(Level.INFO, "Successfully created schema!");
    }

    /**
     * A method which checks if the database schema has been
     * created. Uses a dummy query which would throw an exception if it hasn't
     *
     * @return true if the schema has been created and false if it hasn't
     */
    public static boolean existsDB() {
        try {
            // connect to database
            Class.forName(DRIVER);
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASS);

            // dummy query to check if schema is created
            Statement st = connection.createStatement();
            ResultSet set = st.executeQuery("SELECT * FROM SONGS;");

        } catch (Exception e) {
            // throwing an exception means that the query cant find that table
            // i.e the schema hasn't been created
            return false;
        }
        return true;
    }

    /**
     * A method to check if there are new songs in the music dir.
     *
     * @return true if there are .wav files in the music dir which are not hashed in the DB
     */
    public static int checkForNewSongs(String[] availableSongs) {
        int count = 0;

        logger.log(Level.INFO, "Checking if there are unrecognizable (new) songs...");

        for(String song : availableSongs) {
            if(!isSongInDB(song)) {
                count ++;
            }
        }

        logger.log(Level.INFO, "Done checking for new songs. There is/are " + count + " new song(s)");
        return count;
    }

    /**
     * A method to check if a given song is already hashed in the database.
     *
     * @param song the song to be checked
     * @return true if the song is in the database and false if it isn't
     */
    public static boolean isSongInDB(String song) {
        boolean result = true;
        try {
            // connect to database
            Class.forName(DRIVER);
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASS);

            //init statement
            Statement st = connection.createStatement();

            // check if there is a song with the same name in the DB
            ResultSet count = st.executeQuery("SELECT COUNT(*) FROM SONGS WHERE TITLE = '" + song + "';");
            while (count.next()) {
                result = (count.getInt(1) == 1);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while checking if " + song + " is in DB. \n" + e.toString());
        }

        return result;
    }

    /**
     * A method which inserts an entry in the SONGS table in the database
     * with a give song's name.
     *
     * @param song the TITLE of the new entry in the SONGS TABLE
     */
    public static void initSongInDB(String song) {
        try {
            // connect to database
            Class.forName(DRIVER);
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASS);

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
            Class.forName(DRIVER);

            Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASS);

            Statement st = connection.createStatement();

            ResultSet set = st.executeQuery("SELECT ID_SONG FROM SONGS WHERE TITLE = '" + songName + "'");

            int id = 0;
            while(set.next()) {
                id = set.getInt(1);
            }

            st.executeUpdate("LOCK TABLES HASHES WRITE;");
            for(String hash : hashes) {
                st.executeUpdate("INSERT INTO HASHES (HASH_, SONG_ID) VALUES ('" + hash + "'," + id +");");
            }
            st.executeUpdate("UNLOCK TABLES");

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
