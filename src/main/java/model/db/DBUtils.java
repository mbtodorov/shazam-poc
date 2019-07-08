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
@SuppressWarnings("ConstantConditions")
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
        Connection connection = null;
        try {
            logger.log(Level.INFO, "Checking connection to database...");
            Class.forName(DBConnection.DRIVER);
            //noinspection unused
            connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);
        } catch (Exception e) {
            // throwing an exception would mean unsuccessful connection
            logger.log(Level.SEVERE, "Unable to connect to database! Exiting...");
            System.exit(-1);
            return false;
        } finally {
            try { connection.close(); } catch (Exception e) { /* ignored */ }
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

        Connection connection = null;
        Statement st = null;

        try {
            // connect to the database
            Class.forName(DBConnection.DRIVER);
            connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            // create statement
            st = connection.createStatement();

            st.executeUpdate("CREATE TABLE SONGS (ID_SONG INT(11) NOT NULL " +
                    "AUTO_INCREMENT,TITLE VARCHAR(60) NOT NULL, PRIMARY KEY (ID_SONG));");
            st.executeUpdate("CREATE TABLE HASHES (HASH_ BIGINT NOT NULL, " +
                    "SONG_ID INT(11) NOT NULL);");
            st.executeUpdate("ALTER TABLE HASHES ADD CONSTRAINT VALID FOREIGN KEY (SONG_ID) " +
                    "REFERENCES SONGS (ID_SONG) ON DELETE CASCADE ON UPDATE CASCADE; ");
            st.executeUpdate("ALTER TABLE HASHES ADD INDEX `Hash` USING BTREE (`HASH_`) VISIBLE;");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while creating schema: \n" + e.toString());
        } finally {
            try { st.close(); } catch (Exception e) { /* ignored */ }
            try { connection.close(); } catch (Exception e) { /* ignored */ }
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

        Connection connection = null;
        Statement st = null;
        ResultSet set = null;

        try {
            // connect to database
            Class.forName(DBConnection.DRIVER);
            connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            // dummy query to check if schema is created
            st = connection.createStatement();
            //noinspection unused
            set = st.executeQuery("SELECT * FROM SONGS;");

        } catch (Exception e) {
            // throwing an exception means that the query cant find that table
            // i.e the schema hasn't been created
            return false;
        } finally {
            try { set.close(); } catch (Exception e) { /* ignored */ }
            try { st.close(); } catch (Exception e) { /* ignored */ }
            try { connection.close(); } catch (Exception e) { /* ignored */ }
        }
        return true;
    }

    /**
     * A method that returns a String array of all songs that are in the DB
     * and null if none.
     *
     * @return the songs in the DB
     */
    public static String[] getSongsInDB() {
        String[] result = null;

        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            // connect to database
            Class.forName(DBConnection.DRIVER);
            connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            //init statement
            st = connection.createStatement();

            // count the number of songs
            rs = st.executeQuery("SELECT COUNT(*) FROM SONGS;");
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            // if there are no songs - return null
            if(count == 0) {
                return null;

            } else {
                // init an array of that size
                result = new String[count];

                // get the names of all songs
                int index = 0;
                rs = st.executeQuery("SELECT TITLE FROM SONGS;");

                // add to array
                while (rs.next()) {
                    result[index] = rs.getString(1) + ".wav";
                    index ++;
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while getting all audio from DB" + e.toString());
        } finally {
            try { rs.close(); } catch (Exception e) { /* ignored */ }
            try { st.close(); } catch (Exception e) { /* ignored */ }
            try { connection.close(); } catch (Exception e) { /* ignored */ }
        }

        return result;
    }

    /**
     * A method to check if a given song is already hashed in the database.
     *
     * @param song the song to be checked
     * @return true if the song is in the database and false if it isn't
     */
    @SuppressWarnings("unused")
    public static boolean isSongInDB(String song) {
        boolean result = true;

        Connection connection = null;
        Statement st = null;
        ResultSet count = null;

        try {
            // connect to database
            Class.forName(DBConnection.DRIVER);
            connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

            //init statement
            st = connection.createStatement();

            // check if there is a song with the same name in the DB
            count = st.executeQuery("SELECT COUNT(*) FROM SONGS WHERE TITLE = '" + song + "';");

            while (count.next()) {
                result = (count.getInt(1) == 1);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while checking if " + song + " is in DB. \n" + e.toString());
        } finally {
            try { count.close(); } catch (Exception e) { /* ignored */ }
            try { st.close(); } catch (Exception e) { /* ignored */ }
            try { connection.close(); } catch (Exception e) { /* ignored */ }
        }

        return result;
    }
}
