package eu.vranckaert.episodeWatcher.exception;

/**
 * @author Dirk Vranckaert
 *         Date: 23-sep-2010
 *         Time: 18:44:45
 */
public class ShowAddFailedException extends Exception {
    public ShowAddFailedException(String message, Throwable e) {
        super (message, e);
    }

    public ShowAddFailedException(String message) {
        super(message);
    }
}
