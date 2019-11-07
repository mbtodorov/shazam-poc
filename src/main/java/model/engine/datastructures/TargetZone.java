package model.engine.datastructures;

import java.util.ArrayList;

/**
 * An interface defining what a TargetZone should do.
 *
 * @version 1.0
 * @author Martin Todorov
 */
public interface TargetZone {
    /**
     * This method should return all the hashes which are produced
     * by the target zone.
     *
     * @return The string array containing all hashes.
     */
    ArrayList<Long> getHashes();
}
