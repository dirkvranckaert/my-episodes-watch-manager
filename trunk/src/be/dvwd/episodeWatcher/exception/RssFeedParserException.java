package be.dvwd.episodeWatcher.exception;

public class RssFeedParserException extends RuntimeException {
    public RssFeedParserException(String message, Throwable e) {
        super(message, e);
    }

    public RssFeedParserException(String message) {
        super(message);
    }
    
    public RssFeedParserException(Throwable e) {
    	super(e);
    }
}
