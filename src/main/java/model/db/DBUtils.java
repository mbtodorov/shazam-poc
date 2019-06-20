package main.java.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An utility class which contains utility-related methods for DB communication.
 * It has only static methods and is therefore
 * thread safe. More can be found in the comments of the methods.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class DBUtils {
    // logger
    private static final Logger logger = Logger.getLogger(DBUtils.class.getName());

    /**
     * A simple method to check if the parameters for authentication work.
     * Tries to connect to the database using the static fields above.
     *
     * @return true if the connection was successful and false if it wasn't
     */
    public static boolean checkConnection() {
        try {
            logger.log(Level.INFO, "Checking connection to database...");
            Class.forName(DBConnection.DRIVER);
            //noinspection unused
            Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);
        } catch (Exception e) {
            // throwing an exception would mean unsuccessful connection
            logger.log(Level.SEVERE, "Unable to connect to database! Exiting...");
            System.exit(-1);
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
            Class.forName(DBConnection.DRIVER);
            Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            // create statement
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE SONGS (ID_SONG INT(11) NOT NULL " +
                    "AUTO_INCREMENT,TITLE VARCHAR(60) NOT NULL, PRIMARY KEY (ID_SONG));");
            statement.executeUpdate("CREATE TABLE HASHES (HASH_ BIGINT NOT NULL, " +
                    "SONG_ID INT(11) NOT NULL);");
            statement.executeUpdate("ALTER TABLE HASHES ADD CONSTRAINT VALID FOREIGN KEY (SONG_ID) " +
                    "REFERENCES SONGS (ID_SONG) ON DELETE CASCADE ON UPDATE CASCADE; ");

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
            Class.forName(DBConnection.DRIVER);
            Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            // dummy query to check if schema is created
            Statement st = connection.createStatement();
            //noinspection unused
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSongInDB(String song) {
        boolean result = true;
        try {
            // connect to database
            Class.forName(DBConnection.DRIVER);
            Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

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
}
