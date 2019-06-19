package main.java.model.engine.datastructures;

import java.util.Arrays;

public class MyTargetZone implements TargetZone {
    // global variable - how many points will there be in a target zone
    public static final int ZONE_SIZE = 20;
    // global variable - the amount of points hashed at a time
    public static final int NUM_POINTS = 4;
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
        int fAnchor = anchor.getFrequency();
        int tAnchor = anchor.getTime();
        int fPt1;
        int fPt2;
        int fPt3;
        int fPt4;
        int deltaPt1;
        int deltaPt2;
        int deltaPt3;
        int deltaPt4;

        for(int i = 0; i < ZONE_SIZE/NUM_POINTS; i ++) {
            fPt1 = targetZone[i].getFrequency()                ;
            fPt2 = targetZone[i + 5].getFrequency()            ;
            fPt3 = targetZone[i + 10].getFrequency()            ;
            fPt4 = targetZone[i + 15].getFrequency()            ;
            deltaPt1 = (targetZone[i].getTime()     - tAnchor) ;
            deltaPt2 = (targetZone[i + 5].getTime() - tAnchor) ;
            deltaPt3 =  targetZone[i + 10].getTime() - tAnchor  ;
            deltaPt4 =  targetZone[i + 15].getTime() - tAnchor  ;

            String one = fAnchor + "";
            String two = fPt1 + "";
            String three = fPt2 + "";
            String four = fPt3 + "";
            String five = deltaPt1 + "";
            String six = deltaPt2 + "";
            String seven = deltaPt3 + "";
            String eight = deltaPt4 + "";
            String nine = fPt4 + "";

            one = normalizeString(one);
            two = normalizeString(two);
            three = normalizeString(three);
            four = normalizeString(four);
            five = normalizeString(five);
            six = normalizeString(six);
            seven = normalizeString(seven);
            eight = normalizeString(eight);
            nine = normalizeString(nine);

            result[hashIndex] = ""  + one + two + three + four + nine + five + six + seven + eight;

            hashIndex ++;
        }

        return result;
    }

    private String normalizeString(String s) {
        String out = s;
        while(out.length() < 3) out = "0" + out;
        if(out.length() > 3) out = out.substring(out.length()-3);
        return out;
    }
}
