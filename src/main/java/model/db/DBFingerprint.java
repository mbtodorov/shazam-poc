package main.java.model.db;

import main.java.model.engine.datastructures.MyTargetZone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class which takes care of all fingerprint-related DB statements
 * All of the methods are static and it is thread-safe.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class DBFingerprint {
    // logger
    private static final Logger logger = Logger.getLogger(DBFingerprint.class.getName());

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

    /**
     * A method to insert all of the fingerprints for a song
     * in the DB
     *
     * @param hashes the fingerprints
     * @param songName the source of the fingerprints
     */
    public static void insertFingerprint(long[] hashes, String songName) {
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
            for(long hash : hashes) {
                st.executeUpdate("INSERT INTO HASHES (HASH_, SONG_ID) VALUES (" + hash + "," + id +");");
            }
            st.executeUpdate("UNLOCK TABLES");
            logger.log(Level.INFO, "Done inserting hashes for song " + songName + " (id: " + id + ") in DB!");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while inserting fingerprint " + e);
        }
    }

    /**
     * This method matches the hashes generated from mic or stream input to
     * the ones in the DB. It calculates minimum matches required based ot
     * the hash size and whether or not it is mic input (more tolerable when mic).
     *
     * @param hashes the hashes to be matched
     * @param isMic whether the input is from mic or not
     * @return no match found or the song name with enough matches
     */
    public static String lookForMatches(long[] hashes, boolean isMic) {
        HashMap<Integer, Integer> map = new HashMap<>();

        // calculate the minimum matches required based on hash size, and whether its mic or not
        int toleranceFactor = 12;
        if(isMic) toleranceFactor = 16;
        int hashesPerZone = MyTargetZone.ZONE_SIZE/MyTargetZone.NUM_POINTS;
        int numKeyPoints = hashes.length / hashesPerZone;
        int minimumMatches = numKeyPoints/(MyTargetZone.NUM_POINTS * toleranceFactor);
        if(!isMic && minimumMatches < 4) minimumMatches = 4;
        System.out.println("Minimum matches required: " + minimumMatches);
        synchronized (DBFingerprint.class) {
            try {
                // connect to database
                Class.forName(DBConnection.DRIVER);
                Connection connection = DriverManager.getConnection(DBConnection.URL, DBConnection.USER, DBConnection.PASS);

                // create a statement
                Statement st = connection.createStatement();
                int id;

                System.out.print("matched with: ");
                for (long hash : hashes) {
                    ResultSet set = st.executeQuery("SELECT SONG_ID FROM HASHES WHERE HASH_ = " + hash + ";");
                    while (set != null && set.next()) {
                        id = set.getInt(1);
                        System.out.print(id + " ");
                        if (map.containsKey(id)) {
                            map.put(id, map.get(id) + 1);
                            if(map.get(id) > minimumMatches) {
                                ResultSet sett = st.executeQuery("SELECT TITLE FROM SONGS WHERE ID_SONG = " + id + ";");
                                sett.next();
                                return sett.getString(1);

                            }
                        } else {
                            map.put(id, 1);
                        }
                    }
                }

                System.out.println("\n");

                // find the most matched song
                int max = 0;
                int bestMatchId = 0;
                for (int k : map.keySet()) {
                    if (map.get(k) > max) {
                        max = map.get(k);
                        bestMatchId = k;
                    }
                }

                // get its name
                String result = "";
                if (bestMatchId != 0 && map.get(bestMatchId) > minimumMatches) {
                    ResultSet set = st.executeQuery("SELECT TITLE FROM SONGS WHERE ID_SONG = " + bestMatchId + ";");
                    while (set.next()) {
                        result = set.getString(1);
                    }
                }

                if (!result.equals("")) return result;

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception thrown while trying to find matches " + e );
            }
        }
        return null;
    }
}
