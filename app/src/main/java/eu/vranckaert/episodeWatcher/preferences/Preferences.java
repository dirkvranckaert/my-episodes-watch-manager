package eu.vranckaert.episodeWatcher.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import eu.vranckaert.episodeWatcher.utils.ApplicationUtil;
import eu.vranckaert.episodeWatcher.utils.StringUtils;

public class Preferences {
    public static final String LOG_TAG = Preferences.class.getSimpleName();
    public static final String PREF_NAME = "mewmfacred";

    private static final SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getPreference(Context context, String key) {
        String result = getSharedPreferences(context).getString(key, null);
        return result;
    }

    public static boolean getPreferenceBoolean(Context context, String key, boolean defaultSetting) {
        boolean result = getSharedPreferences(context).getBoolean(key, defaultSetting);
        return result;
    }

    public static void setPreference(Context context, String key, String value) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setPreference(Context context, String key, boolean value) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void removePreference(Context context, String key) {
        Editor editor = getSharedPreferences(context).edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * Check if a preference with a certain key is already set. If not it will set a default value.
     * Otherwise it will do nothing.
     *
     * @param context      The activity requesting the preference.
     * @param key          The key of the preference to look for.
     * @param defaultValue The default value for the preference in case it's not found.
     */
    public static void checkDefaultPreference(Context context, String key, String defaultValue) {
        String result = getPreference(context, key);

        if (result == null || result.equals(StringUtils.EMPTY)) {
            setPreference(context, key, defaultValue);
            Log.e(LOG_TAG, key + "  " + defaultValue);
        }
    }

    public static int getPreferenceInt(Context context, String key) {
        String result = getSharedPreferences(context).getString(key, "0");
        if (result == null || result.equals("")) {
            result = "0";
        }
        return Integer.parseInt(result);
    }

    public static boolean isFirstTime(Context context) {
        String currentVersion = ApplicationUtil.getCurrentApplicationVersion(context);
        boolean isFirstTime = false;

        SharedPreferences preferences = getSharedPreferences(context);
        String knownApplicationVersion = preferences.getString(PreferencesKeys.APPLICATION_VERSION_KEY, null);
        if (knownApplicationVersion == null) {
            isFirstTime = true;
        } else {
            if (!knownApplicationVersion.equals(currentVersion)) {
                isFirstTime = true;
            }
        }

        Editor editor = preferences.edit();
        editor.putString(PreferencesKeys.APPLICATION_VERSION_KEY, currentVersion);
        editor.commit();
        return isFirstTime;
    }
}
