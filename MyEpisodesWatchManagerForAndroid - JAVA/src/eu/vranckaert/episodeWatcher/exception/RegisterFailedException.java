package eu.vranckaert.episodeWatcher.exception;

public class RegisterFailedException extends Exception {
    public RegisterFailedException(String message, Throwable e) {
        super(message, e);
    }

    public RegisterFailedException(String message) {
        super(message);
    }
}
