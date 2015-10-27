package eu.vranckaert.episodeWatcher.exception;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * User: DIRK VRANCKAERT
 * Date: Dec 3, 2010
 * Time: 1:22:29 AM
 */
public class TrackerNotInitializedException extends RuntimeException {
	private static final long serialVersionUID = 3955221711239179599L;

	public TrackerNotInitializedException() {
        super("The " + GoogleAnalyticsTracker.class.getSimpleName() + " instance is not initialized!");
    }
}
