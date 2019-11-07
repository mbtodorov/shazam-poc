package model.engine;

import model.db.DBFingerprint;
import model.engine.datastructures.KeyPoint;

import javax.sound.sampled.*;
import java.io.File;
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
     * Takes a file (wav) and undergoes a series of conversions:
     * low-pass filter for frequencies > 5000 hZ; down-sample to 11025 hZ;
     * convert to mono; hamming window function with size 1024; fft. Finally
     * it extracts a fingerprint and  populates a database if the file is not
     * in the database already.
     *
     * @param songName the file to decode
     * @param isInDB whether it is in the DB already or not
     * @return FFT result for drawing spectrogram
     */
    public static double[][] decodeWav(String songName, boolean isInDB) {
        long start = System.currentTimeMillis(); // used for logging speed of algorithm

        // get the song
        File song = new File("music/" + songName);

        // Step 1: apply low pass filter to wav and convert to byte array

        byte[] audioFiltered = null;
        try {
            // Audio input stream automatically filters the header bytes
            AudioInputStream ais = AudioUtils.lowPassFilterAIS(AudioSystem.getAudioInputStream(song));

            audioFiltered = ais.readAllBytes();

            ais.close();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error streaming song " + song.getName() + " to byte array.");
            logger.log(Level.SEVERE, e.getMessage());
        }

        // Step 2 convert raw stereo to raw mono audio

        byte[] audioMonoFiltered = AudioUtils.convertToMono(audioFiltered);

        // Step 3: down sample audio file to 44.1/4 = 11 025 Hz

        byte[] audioDownSampled = AudioUtils.downSample(audioMonoFiltered);

        // Step 4: convert the byte[] raw audio to double[] raw audio

        double[] finalAudio = AudioUtils.byteToDoubleArr(audioDownSampled);

        // Step 5: apply FFT to the double[] to get the point data needed for a spectrogram

        double[][] FFTResults = AudioUtils.applyFFT(finalAudio);

        // The next part of the algorithm is executed only if the song is not already
        // hashed in the database. That is when this algorithm is called for drawing the spectrogram only

        if(!isInDB) { // The algorithm for populating DB with hashes

            // Step 1: Extract only the key points from the FFT results

            KeyPoint[][] keyPoints = AudioFingerprint.extractKeyPoints(FFTResults);

            // Step 2: get the fingerprints from the song

            long[] hashes = AudioFingerprint.hash(keyPoints, false);

            // Step 3: init an entry for the song in the database

            DBFingerprint.initSongInDB(songName);

            // Step 4: insert the hashes in the DB

            DBFingerprint.insertFingerprint(hashes, songName);
        }

        // log time taken
        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Time taken to decodeWav song (with hashing: " + !isInDB + "): " + song.getName() + ": " + (end-start) + "ms");

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
            // Audio input stream automatically filters the header bytes
            AudioInputStream ais = AudioUtils.lowPassFilterAIS(in);

            audio = ais.readAllBytes();

            ais.close();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error streaming input to byte array.");
            logger.log(Level.SEVERE, e.getMessage());
        }

        // Step 2 : convert to mono if its not from mic

        byte[] audioMono = audio;

        if(!isMic) {
            // Convert stereo to mono
            audioMono = AudioUtils.convertToMono(audio);
        }

        // Step 3 : down sample to 11025 Hz

        byte[] decodedAudio = AudioUtils.downSample(audioMono);

        // Step 4: convert the byte[] raw audio to double[] raw audio

        double[] finalAudio = AudioUtils.byteToDoubleArr(decodedAudio);

        // Step 5: apply FFT to the double[] to get the point data needed for extracting key points

        double[][] FFTResults = AudioUtils.applyFFT(finalAudio);

        // Step 6: extract key points from FFT result

        KeyPoint[][] keyPoints = AudioFingerprint.extractKeyPoints(FFTResults);

        // Step 7: Extract ALL possible hashes from the keypoints

        long[] hashes = AudioFingerprint.hash(keyPoints, true);

        // Step 8: look for matching fingerprints in DB.

        String result = DBFingerprint.lookForMatches(hashes, isMic);

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

