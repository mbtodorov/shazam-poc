package main.java.model.datastructures;

import java.util.Arrays;

public class MyTargetZone implements TargetZone {
    // global variable - how many points will there be in a target zone
    public static final int ZONE_SIZE = 9;
    // global variable - the amount of points hashed at a time
    public static final int NUM_POINTS = 3;
    // global variable - the index of the anchor point
    public static final int ANCHOR_INDEX = 0;

    private KeyPoint anchor;
    private KeyPoint[] targetZone;

    public MyTargetZone(KeyPoint[] points) {
        anchor = points[ANCHOR_INDEX];
        targetZone = Arrays.copyOfRange(points, ANCHOR_INDEX+1, ZONE_SIZE + 1);
    }

    public String[] getHashes() {
        String[] result = new String[ZONE_SIZE/NUM_POINTS];
        int hashIndex = 0;

        // variables needed for hashes
        long fAnchor = anchor.getFrequency();
        int  tAnchor = anchor.getTime();
        long fPt1;
        long fPt2;
        int fPt3;
        int deltaPt1;
        int deltaPt2;
        int deltaPt3;

        for(int i = 0; i <= ZONE_SIZE - NUM_POINTS; i += NUM_POINTS) {
            fPt1 = targetZone[i].getFrequency()                ;
            fPt2 = targetZone[i + 1].getFrequency()            ;
            fPt3 = targetZone[i + 2].getFrequency()            ;
            deltaPt1 = (targetZone[i].getTime()     - tAnchor) ;
            deltaPt2 = (targetZone[i + 1].getTime() - tAnchor) ;
            deltaPt3 =  targetZone[i + 2].getTime() - tAnchor  ;
            long hash = (fAnchor  << 54) +
                    (fPt1     << 45) +
                    (fPt2     << 36) +
                    (fPt3     << 27) +
                    (deltaPt1 << 18) +
                    (deltaPt2 << 9 ) +
                    deltaPt3;

            String one = fAnchor + "";
            String two = fPt1 + "";
            String three = fPt2 + "";
            String four = fPt3 + "";
            String five = deltaPt1 + "";
            String six = deltaPt2 + "";
            String seven = deltaPt3 + "";
            one = normalizeString(one);
            two = normalizeString(two);
            three = normalizeString(three);
            four = normalizeString(four);
            five = normalizeString(five);
            six = normalizeString(six);
            seven = normalizeString(seven);

            result[hashIndex] = ""  + one + two + three + four + five + six + seven;

            hashIndex ++;
        }

        return result;
    }

    private String normalizeString(String s) {
        String out = s;
        while(out.length() < 4) out = "0" + out;
        return out;
    }
}
