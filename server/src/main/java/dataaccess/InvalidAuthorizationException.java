package dataaccess;

public class InvalidAuthorizationException extends DataAccessException {
    public InvalidAuthorizationException(String message) {
        super(message);
    }
    public InvalidAuthorizationException(String message, Throwable ex) {
        super(message, ex);
    }
}
