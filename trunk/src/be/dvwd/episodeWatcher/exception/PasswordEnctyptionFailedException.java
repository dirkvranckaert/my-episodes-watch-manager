package be.dvwd.episodeWatcher.exception;

public class PasswordEnctyptionFailedException extends RuntimeException {
    public PasswordEnctyptionFailedException(String message, Throwable e) {
        super (message, e);
    }
}
