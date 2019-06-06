package server.fingerprint;

import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.sound.IirFilterAudioInputStreamExstrom;
import server.dsts.Complex;
import server.fft.FFT;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the algorithms used to decode songs and
 * generate their fingerprints. It is the heaviest 'engine' class
 *
 * It looks for .wav files in {root dir}/music/*.wav
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class WavFingerprint {
    // logger
    private final static Logger logger = Logger.getLogger(WavFingerprint.class.getName());

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

        return songs.toArray(new String[songs.size()]);
    }

    /**
     * Takes a file (wav) and converts it to Complex[][]
     *
     * @param song the file to decode
     * @return 2D spectrogram points representation
     */
    public Complex[][] decode(File song) {
        byte[] audioStereo = new byte[(int) song.length()];
        boolean isStereo = false;
        try {
            //AudioInputStream ais = AudioSystem.getAudioInputStream(song);
            AudioInputStream ais = IirFilterAudioInputStreamExstrom.getAudioInputStream(AudioSystem.getAudioInputStream(song), FilterPassType.lowpass, 100, 5000, 0);
            isStereo = (ais.getFormat().getChannels() >= 2);
            ais.read(audioStereo);
            ais.close();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Error streaming song" + song.getName() + "to byte array.");
            logger.log(Level.SEVERE, e.getMessage());
        }

        // Step 1: convert to mono @44.1 kHz
        byte[] audioMono;
        if(isStereo) {
            audioMono = convertToMono(audioStereo);
        } else {
            audioMono = audioStereo;
        }

        // Step 3: down sample audio file to 44.1/4 kHz
        // TODO: this

        byte[] audioMonoDownSampled = downSample(audioMono);
        /// maybe the code above is downsampling? idk

        // Step 4: Hamming Window Function
        // TODO: this

        // Step 5: FFT to get spectrogram from 0 to 5 kHz and bin size 10.7 Hz
        // TODO: this

        // Step 6: Generate fingerprint from spectrogram

        // Step 7: Populate DB with fingerprint

        // Window size:
        int winSize = 4096;
        int amountPossible = audioMono.length/winSize;

        // When turning into frequency domain we'll need complex numbers:
        Complex[][] results = new Complex[amountPossible][];

        // For all the chunks:
        for(int times = 0;times < amountPossible; times++) {
            Complex[] complex = new Complex[winSize];
            for(int i = 0;i < winSize;i++) {
                //Put the time domain data into a complex number with imaginary part as 0:
                complex[i] = new Complex(audioMono[(times*winSize)+i], 0);
            }
            //Perform FFT analysis on the chunk:
            results[times] = FFT.fft(complex);
        }

        // return Complex[][] for spectrogram visualization
        return results;
    }

    /**
     * A method to convert a stereo byte[] input to a mono byte[]
     * @param stereo stereo byte[]
     *
     * @return mono byte[]
     */
    private byte[] convertToMono(byte[] stereo) {
        logger.log(Level.INFO, "Converting stereo song to mono...");

        byte[] mono = new byte[stereo.length/2];
        int HI = 1;
        int LO = 0;
        // get average of left and right pairs of bytes (sample size is 4)
        for (int i = 0 ; i < mono.length/2; ++i){
            int left = (stereo[i * 4 + HI] << 8) | (stereo[i * 4 + LO] & 0xff);
            int right = (stereo[i * 4 + 2 + HI] << 8) | (stereo[i * 4 + 2 + LO] & 0xff);
            int avg = (left + right) / 2;
            mono[i * 2 + HI] = (byte)((avg >> 8) & 0xff);
            mono[i * 2 + LO] = (byte)(avg & 0xff);
        }

        // UNCOMMENT THIS TO GENERATE A .WAV MONO FILE FROM ORIGINAL STEREO BYTE[]
        writeWavToSystem(mono);

        logger.log(Level.INFO, "Successfully converted song to mono.");
        return mono;
    }

    /**
     * A method to downsample a 44.1 kHz byte[] to 11.025 kHz
     * takes average of groups by 4. Loses some clarity but is good enough
     * for matching.
     * @param original 44.1 kHz byte[]
     * @return 11.025 kHz byte[]
     */
    private byte[] downSample(byte[] original) {
        logger.log(Level.INFO, "Downsampling song...");
        //number of samples you want to average
        int AVERAGE_SAMPLE_COUNT = 4;
        int avgSample;

        for(int i=0; i<original.length-AVERAGE_SAMPLE_COUNT; i += AVERAGE_SAMPLE_COUNT){
            //variable for storing the values of multiple samples
            avgSample = 0;
            for(int a=0; a<AVERAGE_SAMPLE_COUNT; a++){
                //add up the current and the next n samples
                avgSample += original[i+a];
            }
            //devide by the number of samples to average them
            avgSample /= AVERAGE_SAMPLE_COUNT;
            //replace first sample with the averaged value
            original[i] = (byte) avgSample;
        }

        byte[] downsampled = new byte[original.length/4];

        for(int i = 0; i < downsampled.length; i ++) {
            downsampled[i] = original[i];
        }

        logger.log(Level.INFO, "Song downsampled to " + 44100/AVERAGE_SAMPLE_COUNT + " kHz.");
        return downsampled;
    }

    /**
     * A method to write a byte[] to a .wav file. Used for testing purposes
     * Double-check parameters for audio format
     * @param audio byte[] to be converted to wav
     */
    private void writeWavToSystem(byte[] audio) {
        ByteArrayInputStream leftbais = new ByteArrayInputStream(audio);
        AudioInputStream out = new AudioInputStream(leftbais, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false), audio.length/4);
        try {
            AudioSystem.write(out, AudioFileFormat.Type.WAVE, new File("generated.wav"));
            logger.log(Level.INFO, AudioSystem.getAudioInputStream(new File("generated.wav")).getFormat().toString());
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Error trying to write wav file to disk." + e.getMessage());
        }
    }
}
