package eu.vranckaert.episodeWatcher.exception;

public class PasswordEnctyptionFailedException extends Exception {
    public PasswordEnctyptionFailedException(String message, Throwable e) {
        super (message, e);
    }
}
