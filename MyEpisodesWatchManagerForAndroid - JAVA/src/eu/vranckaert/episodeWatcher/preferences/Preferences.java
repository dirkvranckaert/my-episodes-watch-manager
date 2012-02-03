package eu.vranckaert.episodeWatcher.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import eu.vranckaert.episodeWatcher.utils.ApplicationUtil;
import eu.vranckaert.episodeWatcher.utils.StringUtils;

public class Preferences {
	public static final String LOG_TAG = Preferences.class.getSimpleName();
	public static final String PREF_NAME = "mewmfacred";

    private static final SharedPreferences getSharedPreferences(Activity ac) {
        return ac.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }
	
	public static String getPreference(Activity ac, String key) {
		String result = getSharedPreferences(ac).getString(key, null);
		return result;
	}

    public static boolean getPreferenceBoolean(Activity ac, String key, boolean defaultSetting) {
		boolean result = getSharedPreferences(ac).getBoolean(key, defaultSetting);
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
            Log.e(LOG_TAG, key + "  " + defaultValue);
        }
    }
    
    public static int getPreferenceInt(Activity ac, String key) {
        String result = getSharedPreferences(ac).getString(key, "0");
        if (result == null || result.equals("")) {
            result = "0";
        }
        return Integer.parseInt(result);
    }

    public static boolean isFirstTime(Activity ac) {
        String currentVersion = ApplicationUtil.getCurrentApplicationVersion(ac.getApplicationContext());
        boolean isFirstTime = false;

        SharedPreferences preferences = getSharedPreferences(ac);
        String knownApplicationVersion = preferences.getString(PreferencesKeys.APPLICATION_VERSION_KEY, null);
        if(knownApplicationVersion == null) {
            isFirstTime = true;
        } else {
            if(!knownApplicationVersion.equals(currentVersion)) {
                isFirstTime = true;
            }
        }

        Editor editor = preferences.edit();
        editor.putString(PreferencesKeys.APPLICATION_VERSION_KEY, currentVersion);
        editor.commit();
        return isFirstTime;
    }
}
