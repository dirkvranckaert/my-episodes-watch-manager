package eu.vranckaert.episodeWatcher.twopointo.utils;

import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.SnackbarLayout;
import android.view.View;
import android.widget.TextView;

/**
 * Date: 17/07/15
 * Time: 13:33
 *
 * @author Dirk Vranckaert
 */
public class SnackbarUtil {
    private static View getSnackBarLayout(Snackbar snackbar) {
        if (snackbar != null) {
            return snackbar.getView();
        }
        return null;
    }

    private static TextView getSnackBarTextLayout(Snackbar snackbar) {
        View snackbarView = getSnackBarLayout(snackbar);
        if (snackbarView != null) {
            SnackbarLayout snackbarLayout = (SnackbarLayout) snackbarView;
            return (TextView) snackbarLayout.getChildAt(0);
        }
        return null;
    }

    private static TextView getSnackbarActionLayout(Snackbar snackbar) {
        View snackbarView = getSnackBarLayout(snackbar);
        if (snackbarView != null) {
            SnackbarLayout snackbarLayout = (SnackbarLayout) snackbarView;
            return (TextView) snackbarLayout.getChildAt(1);
        }
        return null;
    }

    public static Snackbar colorSnackBar(Snackbar snackbar, int color) {
        View snackBarView = getSnackBarLayout(snackbar);
        if (snackBarView != null) {
            snackBarView.setBackgroundColor(color);
        }

        return snackbar;
    }

    public static Snackbar colorSnackBarText(Snackbar snackbar, int color) {
        TextView snackBarTextView = getSnackBarTextLayout(snackbar);
        if (snackBarTextView != null) {
            snackBarTextView.setTextColor(color);
        }

        return snackbar;
    }

    public static Snackbar colorSnackBarAction(Snackbar snackbar, int color) {
        TextView snackBarActionView = getSnackbarActionLayout(snackbar);
        if (snackBarActionView != null) {
            snackBarActionView.setTextColor(color);
        }

        return snackbar;
    }
}
