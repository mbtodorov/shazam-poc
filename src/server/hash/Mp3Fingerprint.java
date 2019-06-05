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
     * Scans for songs in {root dir}/music/*.mp3
     *
     * @return a string array with all the songs' names;
     */
    public String[] scanForSongs() {
        File dir = new File("music");
        File[] directoryListing = dir.listFiles();
        ArrayList<String> songs = new ArrayList<>();

        logger.log(Level.INFO, "Looking for songs in folder " + dir.getAbsolutePath());

        assert directoryListing != null;
        for(File file : directoryListing) {
            if(file.getName().endsWith(".mp3")) {
                songs.add(file.getName());
            }
        }

        return songs.toArray(new String[songs.size()]);
    }

    /**
     * Takes a file (mp3) and converts it two Complex[][]
     *
     * @param song the file to decode
     * @return 2D spectrogram points representation
     */
    public Complex[][] decode(File song) {
        // stream the file to a byte array
        byte[] audio = new byte[(int) song.length()];
        try {
            FileInputStream fis = new FileInputStream(song);
            fis.read(audio);
            fis.close();
        }
        catch(IOException e) {
            logger.log(Level.SEVERE, "Error streaming song" + song.getName() + "to byte array.");
        }

        // Window size:
        int winSize = 4096;
        int amountPossible = audio.length/winSize;

        // When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][];

        // For all the chunks:
        for(int times = 0;times < amountPossible; times++) {
            Complex[] complex = new Complex[winSize];
            for(int i = 0;i < winSize;i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audio[(times*winSize)+i], 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }

        return results;
    }
    // TODO: implement hashing algorithm for a single mp3
}
