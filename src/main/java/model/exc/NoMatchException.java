package main.java.model.exc;

/**
 * An exception is thrown if no matching song has been found.
 */
public class NoMatchException extends RuntimeException {
    public NoMatchException(String err) {
        super(err);
    }
}
