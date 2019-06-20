package main.java.model.engine.datastructures;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is my implementation of a target zone.
 * It is not exactly the way shazam does it, but gets the
 * job done for me. The only purpose of this class is to generate
 * fingerprints based on a target zone.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public class MyTargetZone implements TargetZone {
    // global variable - how many points will there be in a target zone
    public static final int ZONE_SIZE = 15;
    // global variable - the amount of points hashed at a time
    public static final int NUM_POINTS = 3;
    // global variable - the index of the anchor point
    private static final int ANCHOR_INDEX = 0;

    // the anchor point
    private KeyPoint anchor;
    // the target zone
    private KeyPoint[] targetZone;

    /**
     * Constructor
     *
     * @param points the target zone + the anchor point
     */
    public MyTargetZone(KeyPoint[] points) {
        anchor = points[ANCHOR_INDEX];
        targetZone = Arrays.copyOfRange(points, ANCHOR_INDEX+1, points.length);
    }

    /**
     * This method generates all of the hashes from the target zone
     *
     * @return an ArrayList of longs containing all the hashes
     */
    public ArrayList<Long> getHashes() {
        ArrayList<Long> resultList = new ArrayList<>();

        // variables needed for hashes
        long fAnchor = anchor.getFrequency();
        int tAnchor = anchor.getTime();
        long fPt1;
        long fPt2;
        long fPt3;
        long deltaPt1;
        long deltaPt2;
        long deltaPt3;

        long temp;
        for(int i = 0; i < ZONE_SIZE/NUM_POINTS; i ++) {
            fPt1 = targetZone[i].getFrequency()                 ;
            fPt2 = targetZone[i + 5].getFrequency()             ;
            fPt3 = targetZone[i + 10].getFrequency()            ;
            deltaPt1 = (targetZone[i].getTime()      - tAnchor) ;
            deltaPt2 = (targetZone[i + 5].getTime()  - tAnchor) ;
            deltaPt3 =  targetZone[i + 10].getTime() - tAnchor  ;

            while(deltaPt1 > 100) deltaPt1 = deltaPt1 % 100;
            while(deltaPt2 > 100) deltaPt2 = deltaPt2 % 100;
            while(deltaPt3 > 100) deltaPt3 = deltaPt3 % 100;

            temp =   deltaPt3        +
                    (deltaPt2 * 100)  +
                    (deltaPt1 * 10000) +
                    (fPt3     * 10000000) +
                    (fPt2     * 10000000000L) +
                    (fPt1     * 10000000000000L) +
                    (fAnchor  * 10000000000000000L);

            resultList.add(temp);
        }

        return resultList;
    }
}
