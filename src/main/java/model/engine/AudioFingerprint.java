package main.java.model.engine;

import main.java.model.engine.datastructures.KeyPoint;
import main.java.model.engine.datastructures.MyTargetZone;
import main.java.model.engine.datastructures.TargetZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        ArrayList<KeyPoint> bin1 = findPeaks(in, 0, 10, 10, 1.18, 0.8);
        ArrayList<KeyPoint> bin2 = findPeaks(in, 10, 20, 12, 1.15, 0.75);
        ArrayList<KeyPoint> bin3 = findPeaks(in, 20, 40, 14, 1.18, 0.7);
        ArrayList<KeyPoint> bin4 = findPeaks(in, 40, 80, 16, 1.28, 0.7);
        ArrayList<KeyPoint> bin5 = findPeaks(in, 80, 160, 18, 1.3, 0.72);
        ArrayList<KeyPoint> bin6 = findPeaks(in, 160, 320, 18, 1.4, 0.7);
        ArrayList<KeyPoint> bin7 = findPeaks(in, 320, 512, 20, 1.49, 0.68);

        // convert to 2D array
        KeyPoint[][] out = {bin1.toArray(new KeyPoint[0]),
                            bin2.toArray(new KeyPoint[0]),
                            bin3.toArray(new KeyPoint[0]),
                            bin4.toArray(new KeyPoint[0]),
                            bin5.toArray(new KeyPoint[0]),
                            bin6.toArray(new KeyPoint[0]),
                            bin7.toArray(new KeyPoint[0])};

        int total = bin1.size() + bin2.size() + bin3.size() + bin4.size() + bin5.size() + bin6.size() + bin7.size();

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
    private static ArrayList<KeyPoint> findPeaks(double[][] in, int binFloor, int binCeil, int size,
                                                 double DEVIATION_FACTOR, double CONSTANT_FACTOR) {
        ArrayList<KeyPoint> result = new ArrayList<>();
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
                        result.add(new KeyPoint(j, k));
                    }
                }
            }
        }

        logger.log(Level.INFO, "Done finding peaks in " + binFloor + " to " + binCeil +
                "! (" + result.size() + " total)");

        return result;
    }

    /**
     * A method to extract fingerprints from
     * the keypoints array.
     *
     * @param points the keypoints from the FFT result
     * @return the fingerprints
     */
    static String[] hash(KeyPoint[][] points, boolean hashAll) {
        // hash code parameters
        int zoneSize = MyTargetZone.ZONE_SIZE;
        int numPts = MyTargetZone.NUM_POINTS;
        int keyPtsLen = points.length;

        int hashSize = keyPtsLen / (zoneSize + 1) * numPts;
        ArrayList<String> resultList = new ArrayList<>();

        // TODO: fix hash size
        logger.log(Level.INFO, "Begin hashing key points (" + keyPtsLen + ") into  " + hashSize + " hashes");

        int increment = 1;
        if(!hashAll) increment += zoneSize;

        for(KeyPoint[] bin : points) {
            for (int i = 0; i < bin.length - (zoneSize + 1); i += increment) {
                KeyPoint[] zone = Arrays.copyOfRange(bin, i, zoneSize + 1 + i);
                TargetZone tz = new MyTargetZone(zone);
                List<String> hashes = Arrays.asList(tz.getHashes());
                resultList.addAll(hashes);
            }
        }

        logger.log(Level.INFO, "Done hashing points (" + keyPtsLen + ") into  " + resultList.size() + " hashes!");

        return resultList.toArray(new String[0]);
    }
}
