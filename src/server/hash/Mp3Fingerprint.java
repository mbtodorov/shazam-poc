package server.hash;

import server.dsts.Complex;
import server.fft.FFT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
public class Mp3Fingerprint {
    // logger
    private final static Logger logger = Logger.getLogger(Mp3Fingerprint.class.getName());

    /**
     * Method to iterate through all mp3 files in the music dir in
     * root folder. Stores results in DB.
     *
     * @return a string array with all the hashed songs' names;
     */
    public String[] generateFingerprints() {
        File dir = new File("music");
        File[] directoryListing = dir.listFiles();
        ArrayList<File> songs = new ArrayList<>();

        logger.log(Level.INFO, "Looking for songs in folder " + dir.getAbsolutePath());

        assert directoryListing != null;
        for(File file : directoryListing) {
            if(file.getName().endsWith(".mp3")) {
                songs.add(file);
            }
        }

        logger.log(Level.INFO, "Iterating through songs found... (" + songs.size() + " total)");

        String[] result = new String[songs.size()];
        int in = 0;
        for(File song : songs) {
            logger.log(Level.INFO, "Decoding song " + song.getName());

            result[in] = song.getName();
            in++;

            byte[] audio = new byte[(int) song.length()];
            try {
                FileInputStream fis = new FileInputStream(song);
                fis.read(audio);
                fis.close();
            }
            catch(IOException e) {
                logger.log(Level.SEVERE, "Error streaming song" + song.getName() + "to byte array.");
            }

            int winSize = 4096;
            int amountPossible = audio.length/winSize;

            //When turning into frequency domain we'll need complex numbers:
            Complex[][] results = new Complex[amountPossible][];

            //For all the chunks:
            for(int times = 0;times < amountPossible; times++) {
                Complex[] complex = new Complex[winSize];
                for(int i = 0;i < winSize;i++) {
                    //Put the time domain data into a complex number with imaginary part as 0:
                    complex[i] = new Complex(audio[(times*winSize)+i], 0);
                }
                //Perform FFT analysis on the chunk:
                results[times] = FFT.fft(complex);
            }

            for(Complex[] c : results) {
                for(Complex cs : c) {
                    System.out.println(cs.toString() + " ");
                }
            }
            logger.log(Level.INFO, song.getName() + " decoded and added to DB");
        }

        logger.log(Level.INFO, "All songs have been hashed!");
        return result;
    }
    // TODO: implement hashing algorithm for a single mp3
}
