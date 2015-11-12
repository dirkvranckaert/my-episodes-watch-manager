package eu.vranckaert.episodeWatcher.twopointo.context;

import android.os.Bundle;
import android.text.TextUtils;
import eu.vranckaert.android.context.AbstractMenuHandler;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.android.context.BaseMenuActivity;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

/**
 * Date: 04/11/15
 * Time: 07:41
 *
 * @author Dirk Vranckaert
 */
public class MainActivity extends BaseMenuActivity {
    private MenuHandler mMenuHandler;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        String language = Preferences.getPreference(this, PreferencesKeys.LANGUAGE_KEY);
        if (!TextUtils.isEmpty(language)) {
            applyLanguage(language);
        }
        super.doCreate(savedInstanceState);
    }

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
