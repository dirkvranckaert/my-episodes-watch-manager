package eu.vranckaert.episodeWatcher.exception;

public class InternetConnectivityException extends Exception {
	private static final long serialVersionUID = 607572637543482670L;

	public InternetConnectivityException(String message, Throwable e) {
        super(message, e);
    }

    public InternetConnectivityException(String message) {
        super(message);
    }
}
