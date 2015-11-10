package eu.vranckaert.android.context;


import android.app.Activity;

/**
 * Date: 12/06/15
 * Time: 11:34
 *
 * @author Dirk Vranckaert
 */
public abstract class AbstractMenuHandler {
    public abstract BaseFragment navigate(Activity activity, int itemId);
}
