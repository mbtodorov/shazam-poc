package main.java.model.fingerprint;

import main.java.model.db.DBModifier;
import main.java.model.db.DBUtils;
import main.java.model.fingerprint.datastructures.KeyPoint;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for decoding songs and extracting their
 * fingerprints. It is a controller class which manages order of execution
 * of the algorithms provided by the AudioUtils class. It has only static
 * methods so it is a thread-safe class
 *
 * It looks for .wav files in {root dir}/music/*.wav
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class AudioDecoder {
    // logger
    private final static Logger logger = Logger.getLogger(AudioDecoder.class.getName());

    /**
     * Scans for songs in {root dir}/music/*.wav
     *
     * @return a string array with all the songs' names;
     */
    public static String[] scanForSongs() {
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

        logger.log(Level.INFO, "Done. Found " + songs.size() + " songs in "+ dir.getAbsolutePath());

        return songs.toArray(new String[0]);
    }

    /**
     * Takes a file (wav) and undergoes a series of conversions:
     * low-pass filter for frequencies > 5000 hZ; down-sample to 11025 hZ;
     * convert to mono; hamming window function with size 1024; fft. Finally
     * it extracts a fingerprint and  populates a database if the file is not
     * in the database already.
     *
     * @param songName the file to decodeWav
     * @return 2D spectrogram points representation
     */
    public static double[][] decodeWav(String songName) {
        long start = System.currentTimeMillis(); // used for logging speed of algorithm

        // This will be needed to determine which part of the algorithm needs to be executed
        boolean withHashing = !DBUtils.isSongInDB(songName);

        // get the song
        File song = new File("music/" + songName);

        // Step 1: apply low pass filter to wav and convert to byte array

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
            ais.close();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error streaming song " + song.getName() + " to byte array.");
            logger.log(Level.SEVERE, e.getMessage());
        }

        // Step 2 convert raw stereo to raw mono audio

        byte[] audioMonoFiltered = AudioUtils.convertToMono(audioStereoFiltered);

        // Step 3: down sample audio file to 44.1/4 = 11 025 Hz

        byte[] audioDownSampled = AudioUtils.downSample(audioMonoFiltered);

        // Step 4: convert the byte[] raw audio to double[] raw audio

        double[] finalAudio = AudioUtils.byteToDoubleArr(audioDownSampled);

        // Step 5: apply FFT to the double[] to get the point data needed for a spectrogram

        double[][] FFTResults = AudioUtils.applyFFT(finalAudio);

        // The next part of the algorithm is executed only if the song is not already
        // hashed in the database, as it is a long process to undergo for every app launch.

        if(withHashing) { // The algorithm for populating DB with hashes

            // Step 1: Extract only the key points from the FFT results

            KeyPoint[] keyPoints = AudioFingerprint.extractKeyPoints(FFTResults);

            // Step 2: get the fingerprints from the song

            String[] hashes = AudioFingerprint.hash(keyPoints);

            synchronized (AudioDecoder.class) { // threads can't write to DB at the same time
                // Step 3: init an entry for the song in the database

                DBModifier.initSongInDB(songName);

                // Step 4: insert the hashes in the DB

                DBModifier.insertHashes(hashes, songName);
            }
        }

        AudioUtils.writeWavToSystem(audioDownSampled, "test");

        // log time taken
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Time taken to decodeWav song (with hashing: " + withHashing + "): " + song.getName() + ": " + (end-start) + "ms");

        // return double[][] for spectrogram visualization
        return FFTResults;
    }

    public static void decodeMic(AudioInputStream ais) {
        byte[] audio = null;

        System.out.println("im hereeeeee alsooo");
        try {
            // TODO: There is some clipping from the filter
            // Audio input stream automatically filters the header bytes
            AudioUtils.lowPassFilterAIS(ais);
            System.out.println("done");
            System.out.println(ais.available());
            audio = ais.readAllBytes();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error streaming song " + " to byte array.");
            logger.log(Level.SEVERE, e.getMessage());
        }

        System.out.println("im hereeeeee");
        AudioUtils.writeWavToSystem(audio, "mic");
    }
}
