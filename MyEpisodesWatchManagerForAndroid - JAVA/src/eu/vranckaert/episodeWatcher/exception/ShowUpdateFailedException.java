package eu.vranckaert.episodeWatcher.exception;

public class ShowUpdateFailedException extends Exception {
	private static final long serialVersionUID = 8862434939814849694L;

	public ShowUpdateFailedException(String message, Throwable e) {
        super (message, e);
    }

    public ShowUpdateFailedException(String message) {
        super(message);
    }
}
