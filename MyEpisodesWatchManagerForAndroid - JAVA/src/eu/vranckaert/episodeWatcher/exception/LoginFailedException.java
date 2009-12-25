package eu.vranckaert.episodeWatcher.exception;

public class LoginFailedException extends Exception {
    public LoginFailedException(String message, Throwable e) {
        super(message, e);
    }

    public LoginFailedException(String message) {
        super(message);
    }
}
