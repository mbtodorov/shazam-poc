package main.java.model.fingerprint;

import main.java.model.fingerprint.datastructures.KeyPoint;
import main.java.model.fingerprint.datastructures.MyTargetZone;
import main.java.model.fingerprint.datastructures.TargetZone;

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
    private final static Logger logger = Logger.getLogger(AudioUtils.class.getName());

    /**
     * A method to extract the keypoints from a double[][].
     * The input double[][] should be the result of a FFT.
     * It looks for keypoints through bin logarithmically (as explained in the
     * article)
     *
     * @param in the result of a FFT in the form of a double[][]
     * @return a double[][] containing 0 for irrelevant points and
     * original values for all relevant points
     */
    public static KeyPoint[] extractKeyPoints(double[][] in) {
        logger.log(Level.INFO, "Begin extracting key points from FFT result...");
        // TODO: implement this better
        // used to sum the highest points for each FFT result
        double sum = 0;
        double average;
        ArrayList<KeyPoint> keyPointArrayList = new ArrayList<>();
        // iterate the first dimension of the array
        for(int i = 0; i < in.length; i ++) {

            // iterate through the 6 logarithmic bands containing bins
            // from: 0-10, 10-20, 20-40, 40-80, etc..
            for(int j = 0; j < 6; j ++) {
                if(j == 0) { // edge case
                    sum += findMax(Arrays.copyOfRange(in[i], 0, 10));
                } else {
                    // logarithmically allocate bands
                    int start = (int) (5*Math.pow(2,j));
                    int end = (int) (5*Math.pow(2, j+1));
                    // edge case
                    if(end == 320) end = in[i].length - 1;
                    sum += findMax(Arrays.copyOfRange(in[i], start, end));
                }
            }

            average = sum/6.5; // TODO: mess around with coefficient - the lower the less points
            sum = 0;

            // iterate and allocate values which pass over the average
            // + a constant value to eliminate any points which are considered due
            // to inherently low bands
            for(int j = in[0].length - 1; j >= 0; j --) {
                if(in[i][j] > average + 0.27) {
                    keyPointArrayList.add(new KeyPoint(i, j));
                }
            }
        }

        KeyPoint[] out = keyPointArrayList.toArray(new KeyPoint[0]);

        logger.log(Level.INFO, "Done extracting keypoints from FFT result!");
        return out;
    }

    /**
     * A quick method to determine the maximum value from
     * a double array
     *
     * @param in a double[]
     * @return the max value from the double[]
     */
    private static double findMax(double[] in) {
        double max = 0.0;
        for (double v : in) {
            if (v > max) max = v;
        }
        return max;
    }

    /**
     * A method to hash the keypoints from the FFT result
     * It returns an array of 64 bit longs containing all hashes
     * 9bits - frequency of anchor points
     * 9bits - frequency of point 1
     * 9bits - frequency of point 2
     * 9bits - frequency of point 3
     * 9bits - delta time between point 1 and anchor
     * 9bits - delta time between point 2 and anchor
     * 9bits - delta time between point 3 and anchor
     * total of 63 bits + 0 in the beginning
     * @param points the keypoints from the FFT result
     * @return a long array containing all generated hashes
     */
    static String[] hash(KeyPoint[] points) {
        // hash code parameters
        int zoneSize = MyTargetZone.ZONE_SIZE;
        int numPts = MyTargetZone.NUM_POINTS;
        int keyPtsLen = points.length;

        reverseFrequencies(points);

        int hashSize = keyPtsLen / (zoneSize + 1) * numPts;
        ArrayList<String> resultList = new ArrayList<>();

        logger.log(Level.INFO, "Begin hashing key points (" + keyPtsLen + ") into  " + hashSize + " hashes");

        for(int i = 0; i < keyPtsLen - (zoneSize + 1); i += (zoneSize + 1)) {
            KeyPoint[] zone = Arrays.copyOfRange(points, i, zoneSize + 1 +i);
            TargetZone tz = new MyTargetZone(zone);
            List<String> hashes = Arrays.asList(tz.getHashes());
            resultList.addAll(hashes);
        }

        logger.log(Level.INFO, "Done hashing points (" + keyPtsLen + ") into  " + resultList.size() + " hashes!");

        return resultList.toArray(new String[0]);
    }

    private static void reverseFrequencies(KeyPoint[] points) {
        int i = 0;
        for(KeyPoint kp : points) {
            kp.setFrequency(511-kp.getFrequency());
        }
    }
}
