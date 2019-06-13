package main.java.model.exc;

/**
 * An exception is thrown if a matching song has been found in DB
 */
public class MatchFoundException extends RuntimeException {
    MatchFoundException(String err) {
        super(err);
    }
}
