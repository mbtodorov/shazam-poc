package main.java.model.engine;

import main.java.model.engine.datastructures.KeyPoint;
import main.java.model.engine.datastructures.MyTargetZone;
import main.java.model.engine.datastructures.TargetZone;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the shazam-specific algorithms for extracting
 * fingerprints from audio files. It contains static methods and it is
 * thus thread-safe
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class AudioFingerprint {
    // logger
    private final static Logger logger = Logger.getLogger(AudioFingerprint.class.getName());

    /**
     * A method to extract the keypoints from a double[][].
     * The input double[][] should be the result of a FFT.
     * It looks for keypoints through logarithmic frequency bins. In a general sense,
     * It is designed to pick out points which are strong in their vicinity. This is
     * how shazam handles robust noise cancellation. The higher the bin the higher the
     * vicinity it looks through.
     *
     * @param in the result of a FFT in the form of a double[][]
     * @return a KeyPoint 2D array where the first  dimension is the frequency bin (0 - 7) and the second are the
     * keypoints from the bin
     */
    public static KeyPoint[][] extractKeyPoints(double[][] in) {
        logger.log(Level.INFO, "Begin extracting key points from FFT result...");

        // extract keypoints from each of the 7 logarithmic bins
        KeyPoint[] bin1 = findPeaks(in, 0, 10, 10, 1.25, 0.8);
        KeyPoint[] bin2 = findPeaks(in, 10, 20, 12, 1.23, 0.75);
        KeyPoint[] bin3 = findPeaks(in, 20, 40, 14, 1.25, 0.7);
        KeyPoint[] bin4 = findPeaks(in, 40, 80, 16, 1.3, 0.7);
        KeyPoint[] bin5 = findPeaks(in, 80, 160, 18, 1.3, 0.74);
        KeyPoint[] bin6 = findPeaks(in, 160, 320, 18, 1.4, 0.7);
        KeyPoint[] bin7 = findPeaks(in, 320, 512, 20, 1.49, 0.68);

        // convert to 2D array
        KeyPoint[][] out = {bin1, bin2, bin3, bin4, bin5, bin6, bin7};

        int total = bin1.length + bin2.length + bin3.length + bin4.length + bin5.length + bin6.length + bin7.length;

        logger.log(Level.INFO, "Done extracting keypoints from FFT result! (" + total + " total)");
        return out;
    }

    /**
     * This is the algorithm which decides which points will be kept as 'peak'
     * and which should be discarded.
     *
     * @param in the FFT result
     * @param binFloor the frequency bin start
     * @param binCeil the frequency bin end
     * @param size the size of the square which will be looked around the point
     * @param DEVIATION_FACTOR a multiplication coefficient
     * @param CONSTANT_FACTOR a constant coefficient (no frequencies below it will be accepted
     * @return an ArrayList containing all key points from the specified bin
     */
    private static KeyPoint[] findPeaks(double[][] in, int binFloor, int binCeil, int size,
                                                 double DEVIATION_FACTOR, double CONSTANT_FACTOR) {
        LinkedList<KeyPoint> result = new LinkedList<>();
        double sum = 0;
        int count = 0;
        double average;

        if(in.length < size) size = in.length;

        logger.log(Level.INFO, "Finding peaks in bin " + binFloor + " to " + binCeil + "...");

        for(int i = 0; i < in.length; i += size) {
            if(i + size > in.length) {
                size = in.length - i;
            }

            for(int j = binFloor; j < binCeil; j ++) {
                for(int k = i; k < i + size; k ++) {
                    if(in[k][j] > 0) {
                        sum += in[k][j];
                        count ++;
                    }
                }
            }

            average = sum / count * DEVIATION_FACTOR;
            sum = 0;
            count = 0;

            for(int j = i; j < i + size; j ++) {
                for(int k = binFloor; k < binCeil; k ++) {
                    if(in[j][k] > average && in[j][k] > CONSTANT_FACTOR) {
                        result.addLast(new KeyPoint(j, k));
                    }
                }
            }
        }

        logger.log(Level.INFO, "Done finding peaks in " + binFloor + " to " + binCeil +
                "! (" + result.size() + " total)");

        return result.toArray(new KeyPoint[0]);
    }

    /**
     * This method extracts fingerprints from a KeyPoint 2D array.
     * the first dimension represents a frequency bin and the second
     * the key points from it. It extracts them bin by bin so the recognition
     * can be more robust. For example the microphone can't detect low frequencies
     * and if there are hashes linked with them none will match the input.
     *
     * @param points the keypoints from the FFT result
     * @param hashAll whether or not to generate hashes regardless of time. This is false when its
     *                decoding a song and true when its decoding input for matching
     * @return the fingerprints
     */
    static long[] hash(KeyPoint[][] points, boolean hashAll) {
        // hash code parameters
        int zoneSize = MyTargetZone.ZONE_SIZE;
        int keyPtsLen = 0;

        for(KeyPoint[] bin : points) {
            keyPtsLen += bin.length;
        }

        LinkedList<Long> resultList = new LinkedList<>();

        logger.log(Level.INFO, "Begin hashing key points (" + keyPtsLen + " total)...");

        int increment = 1;
        if(!hashAll) increment += zoneSize;

        for(KeyPoint[] bin : points) {
            for (int i = 0; i < bin.length - (zoneSize + 1); i += increment) {
                KeyPoint[] zone;
                if(!hashAll && bin.length - (zoneSize + 1) < i) {
                    i = bin.length - (zoneSize + 1);
                    zone = Arrays.copyOfRange(bin, i, bin.length);
                } else {
                    zone = Arrays.copyOfRange(bin, i, zoneSize + 1 + i);
                }
                TargetZone tz = new MyTargetZone(zone);
                resultList.addAll(tz.getHashes());
            }
        }

        logger.log(Level.INFO, "Done hashing points into " + resultList.size() + " hashes!");

        return resultList.stream().mapToLong(Long::longValue).toArray();
    }
}
