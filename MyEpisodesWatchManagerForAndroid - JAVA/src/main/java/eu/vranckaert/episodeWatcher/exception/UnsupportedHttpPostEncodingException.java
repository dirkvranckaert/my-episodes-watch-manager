package eu.vranckaert.episodeWatcher.exception;

public class UnsupportedHttpPostEncodingException extends Exception {
	private static final long serialVersionUID = 4984580975162860528L;

	public UnsupportedHttpPostEncodingException(String message, Throwable e) {
        super(message, e);
    }
}
