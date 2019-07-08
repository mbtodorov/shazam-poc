package main.java.model.engine;

import main.java.model.engine.fft.FFT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

// libs for low-pass filter
import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.sound.IirFilterAudioInputStreamFisher;

/**
 * This class is the main engine class. It provides all algorithms required
 * for audio manipulation. It is a thread-safe class because all of its methods are
 * static.
 *
 * CAUTION: All of the methods are not very dynamic; that is, the order of which
 * they are executed is important. The WavController class executes them in a proper order
 * and thus produces best results. Please check for reference as some methods expect
 * a very specific format for their input.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class AudioUtils {
    // logger
    private final static Logger logger = Logger.getLogger(AudioUtils.class.getName());

    /**
     * A method to apply a low-pass filter to a stream. The filter aims to remove all frequencies
     * > 5 kHz. It uses the dps-collections library.
     *
     * TODO: there is some clipping from the filter
     *
     * @param ais the input audio stream
     * @return An audio input stream which contains the song with a filter applied to it
     */
    static AudioInputStream lowPassFilterAIS(AudioInputStream ais) {
        FilterPassType filterPassType = FilterPassType.lowpass;
        FilterCharacteristicsType filterCharacteristicsType = FilterCharacteristicsType.butterworth;
        int filterOrder = 4;
        double ripple = 0;
        double fcf1 = 4000;
        double fcf2 = 0;
        logger.log(Level.INFO, "Applying low-pass filter to stream");
        AudioInputStream result = IirFilterAudioInputStreamFisher.getAudioInputStream(ais, filterPassType,
                filterCharacteristicsType, filterOrder, ripple, fcf1, fcf2);
        logger.log(Level.INFO, "Successfully applied low-pass filter to stream!");
        return result;
    }

    /**
     * A method to convert a stereo byte[] to mono byte[]
     * It uses the doubleBitWiseCompressionAlgorithm
     *
     * @param in the raw stereo audio
     * @return the raw mono audio
     */
    static byte[] convertToMono(byte[] in) {
        logger.log(Level.INFO, "Converting stereo to mono...");

        byte[] result = doubleBitWiseCompression(in);

        logger.log(Level.INFO, "Successfully converted to mono!");
        return result;
    }

    /**
     * A method to down-sample a 44.1 kHz byte[] to 11.025 kHz
     * takes average of groups by 4. Loses some clarity but is good enough
     * for task.
     *
     * @param original 44.1 kHz byte[]
     * @return 11.025 kHz byte[]
     */
    static byte[] downSample(byte[] original) {
        logger.log(Level.INFO, "Down-sampling...");

        // compress once to down-sample to 22050 and again to down-sample to 11025
        byte[] result = doubleBitWiseCompression(doubleBitWiseCompression(original));

        logger.log(Level.INFO, "Down-sampled to 11025 kHz.");
        return result;
    }

    /**
     * This method implements an algorithm to compress a byte[]
     * exactly by 2. It can used both for mono conversion or down-sampling
     * Compression is done by taking the average of two bytes.
     *
     * @param in byte[] array to be compressed
     * @return compressed byte[] (2x less size)
     */
    private static byte[] doubleBitWiseCompression(byte[] in) {
        byte[] out = new byte[(int) Math.ceil(in.length/2)];
        for (int i = 0 ; i < out.length/2; ++i){
            int left  = (in[i * 4 + 1]     << 8) | (in[i * 4]     & 0xff);
            int right = (in[i * 4 + 2 + 1] << 8) | (in[i * 4 + 2] & 0xff);
            int avg = (left + right) / 2;
            out[i * 2 + 1] = (byte) ((avg >> 8) & 0xff);
            out[i * 2]     = (byte)  (avg       & 0xff);
        }
        return out;
    }

    /**
     * This method converts a byte[] input to a double[]
     *
     * @param in the byte[] to be processed
     * @return a double[] representation of the input
     */
    static double[] byteToDoubleArr(byte[] in) {
        logger.log(Level.INFO, "Converting byte[] to double[]...");

        int new_length = in.length/2;
        double[] out = new double[new_length];

        for (int i = 0; 2*i+1 < in.length; i++){
            out[i] = (short)((in[2*i+1] & 0xff) << 8) | (in[2*i] & 0xff);
        }

        logger.log(Level.INFO, "Successfully converted byte[] to double[]!");
        return out;
    }

    /**
     * A method to apply FFT to a double[] and return a double[][]
     * containing point data required for drawing a spectrogram
     *
     * @param audio the input array
     * @return the output point data
     */
    static double[][] applyFFT(double[] audio) {
        int length = audio.length;

        //initialize parameters for FFT
        int WS = 1024; //WS = window size
        int OF = 1;    //OF = overlap factor
        int windowStep = WS / OF;

        //calculate FFT parameters
        double SR = 11025;
        double time_resolution = WS / SR;
        double frequency_bin = SR / WS;
        double highest_detectable_frequency = SR / 2.0;
        double lowest_detectable_frequency = 5.0 * SR / WS;

        logger.log(Level.INFO, "time_resolution: " + time_resolution * 1000 + " ms \n frequency_bin: " +
                   frequency_bin + " Hz \n highest_detectable_frequency: " + highest_detectable_frequency +
                   " Hz \n lowest_detectable_frequency + " + lowest_detectable_frequency + " Hz");

        //initialize results array
        int nX = (length - WS) / windowStep;
        int nY = WS / 2;
        double[][] results = new double[nX][nY];

        double maxAmp = Double.MIN_VALUE;
        double minAmp = Double.MAX_VALUE;
        double threshold = 1.0;
        double amp_square;

        double[] inputImag = new double[length];

        logger.log(Level.INFO, "Begin applying FFT...");
        for (int i = 0; i < nX; i++) {
            Arrays.fill(inputImag, 0.0);
            double[] WS_array = FFT.fft(Arrays.copyOfRange(audio, i * windowStep, i * windowStep + WS), inputImag, true);
            for (int j = 0; j < nY; j++) {
                assert WS_array != null;
                amp_square = (WS_array[2 * j] * WS_array[2 * j]) + (WS_array[2 * j + 1] * WS_array[2 * j + 1]);
                if (amp_square == 0.0) {
                    results[i][j] = amp_square;
                } else {
                    results[i][j] = 10 * Math.log10(Math.max(amp_square, threshold));
                }

                //find MAX and MIN amplitude
                if (results[i][j] > maxAmp)
                    maxAmp = results[i][j];
                else if (results[i][j] < minAmp)
                    minAmp = results[i][j];

            }
        }


        logger.log(Level.INFO, "FFT applied successfully! \n Maximum amplitude: " +
                   maxAmp +" \n Minimum amplitude: " + minAmp + "\n x: " + results.length + "\n y: " +
                   results[0].length);

        //Normalization
        double diff = maxAmp - minAmp;
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                results[i][j] = (results[i][j] - minAmp) / diff;
            }
        }

        return results;
    }

    /**
     * A method to write a byte[] to a .wav file. Used for testing purposes
     * Double-check parameters for audio format. They are not determined
     * dynamically. They need to be adjusted based on what byte[] you're passing.
     *
     * @param audio byte[] to be converted to wav
     */
    @SuppressWarnings("unused")
    public static void writeWavToSystem(byte[] audio, String filename) {
        ByteArrayInputStream b = new ByteArrayInputStream(audio);
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        int sampleRate = 11025;
        int sampleSizeInBits = 16;
        int channels = 1;
        int frameSize = 2;
        int frameRate = 11025;
        AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize,
                                                  frameRate, false);
        AudioInputStream out = new AudioInputStream(b, audioFormat, audio.length);
        try {
            AudioSystem.write(out, AudioFileFormat.Type.WAVE, new File(filename + ".wav"));
            logger.log(Level.INFO, AudioSystem.getAudioInputStream(new File(filename + ".wav")).getFormat().toString());
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error trying to write wav file to disk." + e.getMessage());
        }
    }

    /**
     * Scans for audio in {root dir}/music/*.wav
     *
     * @return a string array with all the audio' names;
     */
    public static String[] scanForSongs() {
        File dir = new File("music");
        File[] directoryListing = dir.listFiles();
        ArrayList<String> songs = new ArrayList<>();

        logger.log(Level.INFO, "Looking for audio in folder " + dir.getAbsolutePath());

        assert directoryListing != null;
        for(File file : directoryListing) {
            if(file.getName().endsWith(".wav")) {
                songs.add(file.getName());
            }
        }

        logger.log(Level.INFO, "Done. Found " + songs.size() + " audio in "+ dir.getAbsolutePath());

        if(songs.size() > 0) {
            return songs.toArray(new String[0]);
        } else {
            return null;
        }
    }
}