package e2e.exceptions;

public class LoadsTooLongException extends RuntimeException {
    public LoadsTooLongException() {
        super("The load time exceeded the expected duration.");
    }

    public LoadsTooLongException(String message) {
        super(message);
    }
}
