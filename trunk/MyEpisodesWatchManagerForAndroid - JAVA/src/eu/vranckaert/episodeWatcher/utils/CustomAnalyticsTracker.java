package eu.vranckaert.episodeWatcher.utils;

import android.content.Context;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import eu.vranckaert.episodeWatcher.Constants.GoogleAnalyticsTrackerBasicConstants;
import eu.vranckaert.episodeWatcher.enums.CustomTracker;
import eu.vranckaert.episodeWatcher.exception.TrackerNotInitializedException;

/**
 * User: DIRK VRANCKAERT
 * Date: Dec 3, 2010
 * Time: 1:02:30 AM
 */
public class CustomAnalyticsTracker {
    private GoogleAnalyticsTracker tracker;
    private boolean debug = false;

    private CustomAnalyticsTracker() {}

    public static CustomAnalyticsTracker getInstance(Context context) {
        CustomAnalyticsTracker customTracker = new CustomAnalyticsTracker();
        customTracker.debug = GoogleAnalyticsTrackerBasicConstants.DEBUG;

            if(!customTracker.debug) {
                customTracker.tracker = GoogleAnalyticsTracker.getInstance();
                customTracker.tracker.start(GoogleAnalyticsTrackerBasicConstants.WEB_PROPERTY_ID,
                                            GoogleAnalyticsTrackerBasicConstants.DISPATCH_INTERVAL,
                                            context);
            }

        return customTracker;
    }

    public void trackEvent(CustomTracker.Event event) {
        if(!debug) {
            if(tracker == null) {
                throw new TrackerNotInitializedException();
            }

            tracker.trackEvent(event.getCategory(), event.getAction(), "", 0);
        }
    }

    public void trackPageView(CustomTracker.PageView pageView) {
        if(!debug) {
            if(tracker == null) {
                throw new TrackerNotInitializedException();
            }

            tracker.trackPageView(pageView.getPageView());
        }
    }
}
