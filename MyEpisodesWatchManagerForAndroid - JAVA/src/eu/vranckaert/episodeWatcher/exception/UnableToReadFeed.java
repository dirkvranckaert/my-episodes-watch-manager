package eu.vranckaert.episodeWatcher.exception;

import java.io.IOException;

public class UnableToReadFeed extends Throwable {
    public UnableToReadFeed(String message, Throwable e) {
        super(message, e);
    }
}
