package be.dvwd.episodeWatcher.exception;

public class FeedUrlParsingException extends RuntimeException {
    public FeedUrlParsingException(String message, Throwable e) {
        super(message, e);
    }

    public FeedUrlParsingException(String message) {
        super(message);
    }
    
    public FeedUrlParsingException(Throwable e) {
    	super(e);
    }
}
