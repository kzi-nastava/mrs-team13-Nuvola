package Nuvola.Projekatsiit2025.exceptions;

public class RatingTimeExpiredException extends RuntimeException {
    public RatingTimeExpiredException() {
        super("The time window for submitting a rating has expired.");
    }

    public RatingTimeExpiredException(String message) {
        super(message);
    }
}
