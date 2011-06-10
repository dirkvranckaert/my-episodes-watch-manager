package eu.vranckaert.episodeWatcher.exception;

public class FeedUrlBuildingFaildException extends Exception {
	private static final long serialVersionUID = 8160276290228372576L;

	public FeedUrlBuildingFaildException(String message, Throwable e) {
        super (message, e);
    }
}
