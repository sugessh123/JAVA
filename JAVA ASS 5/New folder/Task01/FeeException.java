// Business-level problem that can be shown to the user as a plain message.
public class FeeException extends Exception {
    public FeeException(String message) { super(message); }
    public FeeException(String message, Throwable cause) { super(message, cause); }
}
