package server.fingerprint;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.sound.IirFilterAudioInputStreamFisher;
import server.fft.FFT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * This class is the main engine class. It provides all algorithms required
 * for wav manipulation. It is a thread-safe class because all of its methods are
 * static.
 *
 * CAUTION: All of the methods are not very dynamic; that is, the order of which
 * they are executed is important. The WavController class executes them in a proper order
 * and thus produces best results. Please check for reference as some methods expect
 * a very specific format for their input.
 *
 * It looks for .wav files in {root dir}/music/*.wav
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class AudioUtils {
    // logger
    private final static Logger logger = Logger.getLogger(AudioUtils.class.getName());

    /**
     * A method to apply a lowpass filter to a song. The filter aims to remove all frequencies
     * > 5 kHz. It uses the dps-collections library.
     *
     * @param song the song to which the lowpass filter should be applied
     * @return An audio input stream which contains the song with a filter applied on it
     * @throws Exception filter-specific exception
     */
    public static AudioInputStream lowPassFilterWav (File song) throws Exception {
        FilterPassType filterPassType = FilterPassType.lowpass;
        FilterCharacteristicsType filterCharacteristicsType = FilterCharacteristicsType.butterworth;
        int filterOrder = 4;
        double ripple = 0;
        double fcf1 = 4000;
        double fcf2 = 0;
        logger.log(Level.INFO, "Applying low pass filter to song...");
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(song);
        return IirFilterAudioInputStreamFisher.getAudioInputStream(inputStream, filterPassType,
               filterCharacteristicsType, filterOrder, ripple, fcf1, fcf2);
    }

    /**
     * A method to convert a stereo byte[] to mono byte[]
     * It uses the doubleBitWiseCompressionAlgorithm
     *
     * @param in the raw stereo audio
     * @return the raw mono audio
     */
    public static byte[] convertToMono(byte[] in) {
        logger.log(Level.INFO, "Converting stereo song to mono...");

        byte[] result = doubleBitWiseCompression(in);

        logger.log(Level.INFO, "Successfully converted song to mono!");
        return result;
    }

    /**
     * A method to downsample a 44.1 kHz byte[] to 11.025 kHz
     * takes average of groups by 4. Loses some clarity but is good enough
     * for matching.
     *
     * @param original 44.1 kHz byte[]
     * @return 11.025 kHz byte[]
     */
    public static byte[] downSample(byte[] original) {
        logger.log(Level.INFO, "Downsampling song...");

        byte[] result = AudioUtils.doubleBitWiseCompression(AudioUtils.doubleBitWiseCompression(original));

        logger.log(Level.INFO, "Song downsampled to 11025 kHz.");
        return result;
    }

    /**
     * This method implements an algorithm to compress a byte[]
     * exactly by 2. It can used both for mono conversion or downsampling
     * Compression is done by taking the average of two bytes.
     *
     * @param in byte[] array to be compressed
     * @return compressed byte[] (2x less size)
     */
    public static byte[] doubleBitWiseCompression(byte[] in) {
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
    public static double[] byteToDoubleArr (byte[] in) {
        logger.log(Level.INFO, "Converting byte[] song to double[]...");

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
    public static double[][] applyFFT(double[] audio) {
        // TODO: improve FFT computation time
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
        int nY = WS / 2 + 1;
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
                amp_square = (WS_array[2 * j] * WS_array[2 * j]) + (WS_array[2 * j + 1] * WS_array[2 * j + 1]);
                if (amp_square == 0.0) {
                    results[i][j] = amp_square;
                } else {
                    results[i][nY - j - 1] = 10 * Math.log10(Math.max(amp_square, threshold));
                }

                //find MAX and MIN amplitude
                if (results[i][j] > maxAmp)
                    maxAmp = results[i][j];
                else if (results[i][j] < minAmp)
                    minAmp = results[i][j];

            }
        }

        logger.log(Level.INFO, "FFT applied successfully! \n Maximum amplitude: " +
                   maxAmp +" \n Minimum amplitude: " + minAmp);

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
    public static void writeWavToSystem(byte[] audio, String filename) {
        ByteArrayInputStream bais = new ByteArrayInputStream(audio);
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        int sampleRate = 11025;
        int sampleSizeInBits = 16;
        int channels = 1;
        int frameSize = 2;
        int frameRate = 11025;
        boolean bigEndian = false;
        AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize,
                                                  frameRate, bigEndian);
        AudioInputStream out = new AudioInputStream(bais, audioFormat, audio.length);
        try {
            AudioSystem.write(out, AudioFileFormat.Type.WAVE, new File(filename + ".wav"));
            logger.log(Level.INFO, AudioSystem.getAudioInputStream(new File(filename + ".wav")).getFormat().toString());
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error trying to write wav file to disk." + e.getMessage());
        }
    }
}