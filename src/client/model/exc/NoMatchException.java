package client.model.exc;

public class NoMatchException extends RuntimeException {
    public NoMatchException(String err) {
        super(err);
    }
}
