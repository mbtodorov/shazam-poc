package server.fingerprint;

import java.util.Arrays;
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
    public static double[][] extractKeyPoints(double[][] in) {
        logger.log(Level.INFO, "Begin extracting key points from FFT result...");
        // TODO: implement this better
        // init the result array; irrelevant points will be kept as 0.0
        double[][] out = new double[in.length][in[0].length];
        // used to sum the highest points for each FFT result
        double sum = 0;
        double average;
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
            for(int j = 0; j < in[0].length; j ++) {
                if(in[i][j] > average + 0.27) {
                    out[i][j] = in[i][j];
                }
            }
        }
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
}
