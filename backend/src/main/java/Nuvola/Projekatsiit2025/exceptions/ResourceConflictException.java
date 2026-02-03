package Nuvola.Projekatsiit2025.exceptions;

public class ResourceConflictException extends RuntimeException {
    private static final long serialVersionUID = 1791564636123821405L;

    private String username;

    public ResourceConflictException(String username, String message) {
        super(message);
        setUsername(username);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
