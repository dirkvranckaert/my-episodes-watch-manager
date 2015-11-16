package eu.vranckaert.episodeWatcher.twopointo.threading;

import android.app.Activity;
import android.content.Context;
import eu.vranckaert.android.threading.CustomTask;
import eu.vranckaert.android.threading.ErrorMapping;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.exception.FeedUrlParsingException;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowAddFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowUpdateFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;

/**
 * Date: 16/11/15
 * Time: 17:56
 *
 * @author Dirk Vranckaert
 */
public abstract class MyEpisodesTask<T extends Object> extends CustomTask<T> {
    public MyEpisodesTask(Activity activity) {
        super(activity);
        setShowErrorDialog(true);
    }

    public MyEpisodesTask(Context context) {
        super(context);
        setShowErrorDialog(true);
    }

    @Override
    public ErrorMapping getErrorMapping(Exception e) {
        ErrorMapping.Builder builder = new ErrorMapping.Builder().setTitle(getString(R.string.exceptionDialogTitle));

        if (e instanceof InternetConnectivityException) {
            return builder.setMessage(getString(R.string.networkIssues)).build();
        } else if (e instanceof UnsupportedHttpPostEncodingException) {
            return builder.setMessage(getString(R.string.networkIssues)).build();
        } else if (e instanceof FeedUrlParsingException) {
            return builder.setMessage(getString(R.string.watchListUnableToReadFeed)).build();
        } else if (e instanceof LoginFailedException) {
            return builder.setMessage(getString(R.string.networkIssues)).build();
        } else if (e instanceof ShowUpdateFailedException) {
            return builder.setMessage(getString(R.string.watchListUnableToMarkWatched)).build();
        } else if (e instanceof ShowAddFailedException) {
            return builder.setMessage(getString(R.string.searchShowUnabletoAdd)).build();
        } else {
            return builder.setMessage(getString(R.string.defaultExceptionMessage)).build();
        }
    }
}
