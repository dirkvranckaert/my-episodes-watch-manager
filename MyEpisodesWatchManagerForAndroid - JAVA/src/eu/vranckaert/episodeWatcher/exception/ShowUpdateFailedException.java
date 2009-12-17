package eu.vranckaert.episodeWatcher.exception;

import java.io.IOException;

public class ShowUpdateFailedException extends Throwable {
    public ShowUpdateFailedException(String message, Throwable e) {
        super (message, e);
    }

    public ShowUpdateFailedException(String message) {
        super(message);
    }
}
