package client.model;

import client.model.exc.MatchFoundException;
import client.model.exc.NoMatchException;

public class MicListener {

    private static final String MATCH_FOUND, NO_MATCH_FOUND;
    static {
        MATCH_FOUND = "Success! A match has been found: ";
        NO_MATCH_FOUND = "No match has been found. Try Again.";
    }

    public static void listen() throws NoMatchException, MatchFoundException {
        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException ex) {

        }
        throw new NoMatchException(NO_MATCH_FOUND);
    }
}
