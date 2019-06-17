package main.java.model.fingerprint;

import main.java.model.db.DBFingerprint;
import main.java.model.db.DBUtils;
import main.java.model.fingerprint.datastructures.KeyPoint;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for decoding audio and extracting
 * fingerprints and decoding audio and matching fingerprints.
 * It is the main controller class which manages order of execution
 * of the algorithms provided by the AudioUtils class. It has only static
 * methods so it is a thread-safe class
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
            AudioInputStream ais = AudioUtils.lowPassFilterAIS(AudioSystem.getAudioInputStream(song));

            audioStereoFiltered = new byte[ais.available()];
            audioStereoFiltered = ais.readAllBytes();

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

            synchronized (AudioDecoder.class) { // concurrent can't write to DB at the same time
                // Step 3: init an entry for the song in the database

                DBFingerprint.initSongInDB(songName);

                // Step 4: insert the hashes in the DB

                DBFingerprint.insertHashes(hashes, songName);
            }
        }

        //AudioUtils.writeWavToSystem(audioDownSampled, "test");

        // log time taken
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Time taken to decodeWav song (with hashing: " + withHashing + "): " + song.getName() + ": " + (end-start) + "ms");

        // return double[][] for spectrogram visualization
        return FFTResults;
    }

    /**
     * This method is the main matching method. It takes an input stream,
     * which comes from a microphone or from a .wav file and undergoes a series of
     * computations before finally querying the database for matches.
     *
     * @param in the input stream which is trying to be matched
     * @param isMic whether the stream is coming from a mic or not
     * @return null if there were no matches found and a matched song name if there
     * was a match
     */
    public static String decodeStreamAndMatch(AudioInputStream in, boolean isMic) {
        long start = System.currentTimeMillis(); // used for logging speed of algorithm

        // an array to store the raw audio file
        byte[] audio = null;

        // Step 1 : apply low-pass filter to the stream and convert to byte array

        try {
            // TODO: There is some clipping from the filter
            // Audio input stream automatically filters the header bytes
            AudioInputStream ais = AudioUtils.lowPassFilterAIS(in);

            audio = new byte[ais.available()];
            audio = ais.readAllBytes();

            ais.close();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error streaming input to byte array.");
            logger.log(Level.SEVERE, e.getMessage());
        }

        // an array to store the final raw file
        byte[] decodedAudio;

        // Step 2: Process raw file (if necessary)
        // The algorithm needs to do more computation if the source is not a microphone.

        if(!isMic) {

            // Step 1 : convert stereo to mono

            byte[] audioMono = AudioUtils.convertToMono(audio);

            // Step 2: down sample audio file to 44.1/4 = 11 025 Hz

            decodedAudio = AudioUtils.downSample(audioMono);

        } else {
            // if the source is a microphone there is no extra computation needed
            decodedAudio = audio;
        }

        // Step 3: convert the byte[] raw audio to double[] raw audio

        assert decodedAudio != null;
        double[] finalAudio = AudioUtils.byteToDoubleArr(decodedAudio);

        // Step 4: apply FFT to the double[] to get the point data needed for extracting key points

        double[][] FFTResults = AudioUtils.applyFFT(finalAudio);

        // Step 5: extract key points from FFT result

        KeyPoint[] keyPoints = AudioFingerprint.extractKeyPoints(FFTResults);

        // Step 6: Extract ALL possible hashes from the keypoints

        String[] hashes = AudioFingerprint.hashAll(keyPoints);

        // Step 7: look for matching fingerprints in DB.

        String result = DBFingerprint.lookForMatches(hashes);

        // log time taken
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Time taken to decode input stream: " + (end-start) + "ms");

        return result;
    }

    /**
     * A method used to check the format of a file. It is used when
     * the user wants to match using a file input stream
     *
     * @param input the file to check the format for
     * @return true of format is OK and false uif not
     */
    public static boolean checkFormat(File input) {
        logger.log(Level.INFO, "Checking format of file...");
        AudioInputStream ais;
        try {
            ais = AudioSystem.getAudioInputStream(input);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception thrown while checking format " + e.toString());
            return false;
        }
        return ais.getFormat().toString().equals(getSupportedFormat().toString());
    }

    /**
     * Defines the supported format of file input streams matching
     *
     * @return the format that is supported for matching
     */
    public static AudioFormat getSupportedFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        int sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;
        int frameSize = 4;
        int frameRate = 44100;
        return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize,
                frameRate, false);
    }

}
