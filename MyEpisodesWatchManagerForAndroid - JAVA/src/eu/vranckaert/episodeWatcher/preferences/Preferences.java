package eu.vranckaert.episodeWatcher.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import eu.vranckaert.episodeWatcher.utils.StringUtils;

public class Preferences {
	public static final String LOG_TAG = "MyEpisodesWatchManager";
	public static final String PREF_NAME = "mewmfacred";

    private static final SharedPreferences getSharedPreferences(Activity ac) {
        return ac.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }
	
	public static String getPreference(Activity ac, String key) {
		String result = getSharedPreferences(ac).getString(key, null);
		return result;
	}

    public static boolean getPreferenceBoolean(Activity ac, String key) {
		boolean result = getSharedPreferences(ac).getBoolean(key, true);
		return result;
    }
	
	public static void setPreference(Activity ac, String key, String value) {
		Editor editor = getSharedPreferences(ac).edit();
		editor.putString(key, value);
		editor.commit();
	}

    public static void setPreference(Activity ac, String key, boolean value) {
		Editor editor = getSharedPreferences(ac).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public static void removePreference(Activity ac, String key) {
		Editor editor = getSharedPreferences(ac).edit();
		editor.remove(key);
		editor.commit();
	}

    /**
     * Check if a preference with a certain key is already set. If not it will set a default value.
     * Otherwise it will do nothing.
     * @param ac The activity requesting the preference.
     * @param key The key of the preference to look for.
     * @param defaultValue The default value for the preference in case it's not found.
     */
    public static void checkDefaultPreference(Activity ac, String key, String defaultValue) {
        String result = getPreference(ac, key);

        if (result == null || result.equals(StringUtils.EMPTY)) {
            setPreference(ac, key, defaultValue);
        }
    }
}
