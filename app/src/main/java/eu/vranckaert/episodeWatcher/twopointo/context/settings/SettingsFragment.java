package eu.vranckaert.episodeWatcher.twopointo.context.settings;

import android.os.Bundle;
import eu.vranckaert.android.context.BasePreferenceFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.preferences.Preferences;

/**
 * Date: 12/11/15
 * Time: 11:11
 *
 * @author Dirk Vranckaert
 */
public class SettingsFragment extends BasePreferenceFragment {

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.preferences);
    }

    @Override
    public void doCreatePreferences(Bundle savedInstanceState, String rootkey) {
        getPreferenceManager().setSharedPreferencesName(Preferences.PREF_NAME);
        addPreferencesFromResource(R.xml.settings);
    }
}
