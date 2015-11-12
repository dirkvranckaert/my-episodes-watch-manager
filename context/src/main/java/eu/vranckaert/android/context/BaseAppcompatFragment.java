package eu.vranckaert.android.context;

import android.support.v4.app.Fragment;

/**
 * Date: 12/11/15
 * Time: 12:09
 *
 * @author Dirk Vranckaert
 */
public interface BaseAppcompatFragment {
    Fragment get();

    boolean isStartedFromOutside();

    String getName();

    void setTopLevelFragment(boolean topLevelFragment);
}
