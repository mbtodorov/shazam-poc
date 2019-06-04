package client.model.exc;

public class MatchFoundException extends RuntimeException {
    MatchFoundException(String err) {
        super(err);
    }
}
