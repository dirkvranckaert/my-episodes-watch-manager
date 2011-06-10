package eu.vranckaert.episodeWatcher.exception;

public class RssFeedParserException extends Exception {
	private static final long serialVersionUID = -2533967871783141537L;

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
