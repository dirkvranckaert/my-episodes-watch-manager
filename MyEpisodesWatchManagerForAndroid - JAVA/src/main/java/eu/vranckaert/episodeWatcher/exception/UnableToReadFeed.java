package eu.vranckaert.episodeWatcher.exception;

public class UnableToReadFeed extends Exception {
	private static final long serialVersionUID = -7074275760455631763L;

	public UnableToReadFeed(String message, Throwable e) {
        super(message, e);
    }
}
