package eu.vranckaert.episodeWatcher.exception;

public class PasswordEnctyptionFailedException extends Exception {
	private static final long serialVersionUID = -7843750130846646817L;

	public PasswordEnctyptionFailedException(String message, Throwable e) {
        super (message, e);
    }
}
