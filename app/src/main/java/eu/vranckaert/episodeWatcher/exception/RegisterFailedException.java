package eu.vranckaert.episodeWatcher.exception;

public class RegisterFailedException extends Exception {
	private static final long serialVersionUID = -6413054925523332396L;

	public RegisterFailedException(String message, Throwable e) {
        super(message, e);
    }

    public RegisterFailedException(String message) {
        super(message);
    }
}
