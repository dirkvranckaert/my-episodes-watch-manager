package eu.vranckaert.episodeWatcher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {
	public static final String LOG_TAG = "MyEpisodesWatchManager";
	private static final String PREF_NAME = "mewmfacred";
	
	public static String getPreference(Activity ac, String key) {
		SharedPreferences settings = ac.getSharedPreferences(Preferences.PREF_NAME, Activity.MODE_PRIVATE);
		String result = settings.getString(key, null);		
		return result;
	}
	
	public static void setPreference(Activity ac, String key, String value) {
		SharedPreferences settings = ac.getSharedPreferences(Preferences.PREF_NAME, Activity.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static void removePreference(Activity ac, String key) {
		SharedPreferences settings = ac.getSharedPreferences(Preferences.PREF_NAME, Activity.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.remove(key);
		editor.commit();
	}
}
