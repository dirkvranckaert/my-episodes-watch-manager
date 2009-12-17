package eu.vranckaert.episodeWatcher.exception;

public class UnsupportedHttpPostEncodingException extends RuntimeException {
    public UnsupportedHttpPostEncodingException(String message, Throwable e) {
        super(message, e);
    }
}
