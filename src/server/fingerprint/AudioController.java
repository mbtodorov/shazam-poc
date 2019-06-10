package server.fingerprint;

import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for decoding songs and extracting their
 * fingerprints. It is a controller class which manages order of execution
 * of the algorithms provided by the AudioUtils class.
 *
 * It looks for .wav files in {root dir}/music/*.wav
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class AudioController {
    // logger
    private final static Logger logger = Logger.getLogger(AudioController.class.getName());

    /**
     * Scans for songs in {root dir}/music/*.wav
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
            if(file.getName().endsWith(".wav")) {
                songs.add(file.getName());
            }
        }

        return songs.toArray(new String[0]);
    }

    /**
     * Takes a file (wav) and undergoes a series of conversions:
     * lowpass filter for frequencies > 5000 hZ; downsample to 11025 hZ;
     * convert to mono; hamming window function with size 1024; fft. Finally
     * it extracts a fingerprint and  populates a database.
     *
     * @param song the file to decode
     * @return 2D spectrogram points representation
     */
    public double[][] decode(File song) {
        long start = System.currentTimeMillis();
        // Step 1: apply low pass filter to wav and convert to byte[] array

        byte[] audioStereoFiltered = null;
        try {
            // TODO: There is some clipping from the filter
            // Audio input stream automatically filters the header bytes
            AudioInputStream ais = AudioUtils.lowPassFilterWav(song);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(ais, baos);
            baos.flush();
            baos.close();
            audioStereoFiltered = baos.toByteArray();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error streaming song" + song.getName() + "to byte array.");
            logger.log(Level.SEVERE, e.getMessage());
        }

        // Step 2: down sample audio file to 44.1/4 = 11 025 Hz

        byte[] audioDownSampled = AudioUtils.downSample(audioStereoFiltered);

        // Step 3: convert to mono and get it as a double array

        double[] finalAudio = AudioUtils.getDoubleMonoArray(audioDownSampled);

        // Step 4: FFT to get frequencies from the byte array. After this we
        // will have the spectrogram of the .wav and we can start fingerprinting

        double[][] results = AudioUtils.applyFFT(finalAudio);

        // Step 6: Generate fingerprint from spectrogram

        // Step 7: Populate DB with fingerprint

        // log time taken
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Time take to decode song " + song.getName() + ": " + (end-start) + "ms");

        // return double[][] for spectrogram visualization */
        return results;
    }
}
