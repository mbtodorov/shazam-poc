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
     * It looks for keypoints through bin logarithmically (as explained in the
     * article)
     *
     * @param in the result of a FFT in the form of a double[][]
     * @return a double[][] containing 0 for irrelevant points and
     * original values for all relevant points
     */
    @SuppressWarnings("Duplicates")
    public static KeyPoint[] extractKeyPoints(double[][] in) {
        logger.log(Level.INFO, "Begin extracting key points from FFT result...");
        // TODO: implement this better
        // used to sum the highest points for each FFT result
        double sum = 0;
        double average;
        int count = 0;
        double temp;


        ArrayList<KeyPoint> keyPointArrayList = new ArrayList<>();
        // iterate the first dimension of the array
        for(int i = 0; i < in.length; i ++) {

            // iterate through the 6 logarithmic bands containing bins
            // from: 0-10, 10-20, 20-40, 40-80, etc..
            for(int j = 0; j < 6; j ++) {
                if(j == 0) { // edge case
                    sum += findMax(Arrays.copyOfRange(in[i], 0, 9));
                } else {
                    // logarithmically allocate bands
                    int start = (int) (5*Math.pow(2,j));
                    int end = (int) (5*Math.pow(2, j+1)) - 1;
                    // edge case
                    if(end == 319) end = in[i].length ;
                    sum += findMax(Arrays.copyOfRange(in[i], start, end));
                }
            }

            average = sum/6;
            sum = 0;

            // iterate and allocate values which pass over the average
            // + a constant value to eliminate any points which are considered due
            // to inherently low bands
            for(int j = in[0].length - 1; j >= 0; j --) {
                if(in[i][j] > average + 0.065) {
                    keyPointArrayList.add(new KeyPoint(i, j));
                }
            }
        }

        KeyPoint[] out = keyPointArrayList.toArray(new KeyPoint[0]);

        logger.log(Level.INFO, "Done extracting keypoints from FFT result! (" + out.length + " total)");
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
     * A method to extract fingerprints from
     * the keypoints array.
     *
     * @param points the keypoints from the FFT result
     * @return the fingerprints
     */
    static String[] hash(KeyPoint[] points, boolean hashAll) {
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

        for(int i = 0; i < keyPtsLen - (zoneSize + 1); i += increment) {
            KeyPoint[] zone = Arrays.copyOfRange(points, i, zoneSize + 1 +i);
            TargetZone tz = new MyTargetZone(zone);
            List<String> hashes = Arrays.asList(tz.getHashes());
            resultList.addAll(hashes);
        }

        logger.log(Level.INFO, "Done hashing points (" + keyPtsLen + ") into  " + resultList.size() + " hashes!");

        return resultList.toArray(new String[0]);
    }
}
