package eu.vranckaert.episodeWatcher.exception;

import java.net.MalformedURLException;

public class FeedUrlBuildingFaildException extends RuntimeException {
    public FeedUrlBuildingFaildException(String message, Throwable e) {
        super (message, e);
    }
}
