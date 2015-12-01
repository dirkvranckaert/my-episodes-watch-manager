package eu.vranckaert.episodeWatcher.twopointo.context.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import eu.vranckaert.android.context.BasePreferenceFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.CacheService;
import eu.vranckaert.episodeWatcher.twopointo.context.NavigationManager;

/**
 * Date: 12/11/15
 * Time: 11:11
 *
 * @author Dirk Vranckaert
 */
public class SettingsFragment extends BasePreferenceFragment implements OnSharedPreferenceChangeListener {
    private boolean mLanguageChanged = false;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.preferences);
    }

    @Override
    public void doCreatePreferences(Bundle savedInstanceState, String rootkey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(Preferences.PREF_NAME);
        preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getContext() == null) {
            return;
        }

        if (PreferencesKeys.LANGUAGE_KEY.equals(key)) {
            String language = Preferences.getPreference(getContext(), key);
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            applyLanguage(language);

            if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
                getActivity().recreate();
            } else {
                NavigationManager.restartApplication(getActivity(), true);
            }
        } else if (PreferencesKeys.CACHE_EPISODES.equals(key)) {
            boolean caching = Preferences.getPreferenceBoolean(getContext(), key, true);
            if (!caching) {
                CacheService.clearEpisodeCache();
            }
        }
    }
}
