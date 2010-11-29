package eu.vranckaert.episodeWatcher.exception;

public class InternetConnectivityException extends Exception {
    public InternetConnectivityException(String message, Throwable e) {
        super(message, e);
    }

    public InternetConnectivityException(String message) {
        super(message);
    }
}
