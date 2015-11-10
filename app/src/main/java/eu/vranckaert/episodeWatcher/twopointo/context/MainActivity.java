package eu.vranckaert.episodeWatcher.twopointo.context;

import eu.vranckaert.android.context.AbstractMenuHandler;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.android.context.BaseMenuActivity;

/**
 * Date: 04/11/15
 * Time: 07:41
 *
 * @author Dirk Vranckaert
 */
public class MainActivity extends BaseMenuActivity {
    private MenuHandler mMenuHandler;

    @Override
    public int getHeaderResId() {
        return R.layout.drawer_header;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.drawer;
    }

    @Override
    public AbstractMenuHandler getMenuHandler() {
        if (mMenuHandler == null) {
            mMenuHandler = new MenuHandler();
        }

        return mMenuHandler;
    }

    @Override
    protected int getDefaultMenuItem() {
        return R.id.drawer_item_episodes;
    }


}
