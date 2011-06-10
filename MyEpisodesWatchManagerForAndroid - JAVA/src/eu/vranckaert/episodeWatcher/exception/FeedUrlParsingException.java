package eu.vranckaert.episodeWatcher.exception;

public class FeedUrlParsingException extends Exception {
	private static final long serialVersionUID = -1685121429609745741L;

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
