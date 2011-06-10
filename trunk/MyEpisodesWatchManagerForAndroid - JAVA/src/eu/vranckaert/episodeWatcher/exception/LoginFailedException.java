package eu.vranckaert.episodeWatcher.exception;

public class LoginFailedException extends Exception {
	private static final long serialVersionUID = -2941019709358675926L;

	public LoginFailedException(String message, Throwable e) {
        super(message, e);
    }

    public LoginFailedException(String message) {
        super(message);
    }
}
