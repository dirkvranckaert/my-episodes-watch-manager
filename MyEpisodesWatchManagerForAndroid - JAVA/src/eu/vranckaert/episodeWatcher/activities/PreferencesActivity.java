package eu.vranckaert.episodeWatcher.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

/**
 * @author Dirk Vranckaert
 *         Date: 13-mei-2010
 *         Time: 19:34:14
 */
public class PreferencesActivity extends PreferenceActivity {
    private static final int RELAOD_DIALOG = 0;
    private boolean refreshDialog;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preferencesmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
            case R.id.closePreferences:
                finish();
                return true;
        }
		return false;
	}

    @Override
    public void onCreate(Bundle savedInstance) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
    	super.onCreate(savedInstance);
        
        refreshDialog = false;
        super.setTitle(R.string.preferences);
        
        getPreferenceManager().setSharedPreferencesName(Preferences.PREF_NAME);
        setPreferenceScreen(createPreferenceScreen());
    }

    private PreferenceScreen createPreferenceScreen() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        CheckBoxPreference passwordPref = new CheckBoxPreference(this);
        passwordPref.setDefaultValue(true);
        passwordPref.setKey(PreferencesKeys.STORE_PASSWORD_KEY);
        passwordPref.setTitle(R.string.storePasswordPrompt);
        root.addPreference(passwordPref);

        ListPreference showOrderingPref = new ListPreference(this);
        showOrderingPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				refreshDialog = true;
				return true;
			}
        });
        showOrderingPref.setKey(PreferencesKeys.SHOW_SORTING_KEY);
        showOrderingPref.setTitle(R.string.showOrderPrompt);
        showOrderingPref.setEntries(R.array.showOrderOptions);
        showOrderingPref.setEntryValues(R.array.showOrderOptionsValues);
        root.addPreference(showOrderingPref);

        ListPreference episodeOrderingPref = new ListPreference(this);
        episodeOrderingPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				refreshDialog = true;
				return true;
			}
        });
        episodeOrderingPref.setKey(PreferencesKeys.EPISODE_SORTING_KEY);
        episodeOrderingPref.setTitle(R.string.episodeOrderPrompt);
        episodeOrderingPref.setSummary(R.string.episodeOrderPromptExtra);
        episodeOrderingPref.setEntries(R.array.episodeOrderOptions);
        episodeOrderingPref.setEntryValues(R.array.episodeOrderOptionsValues);
        root.addPreference(episodeOrderingPref);

        ListPreference openDefaultTabPref = new ListPreference(this);
        openDefaultTabPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				refreshDialog = true;
				return true;
			}
        });
        openDefaultTabPref.setKey(PreferencesKeys.OPEN_DEFAULT_TAB_KEY);
        openDefaultTabPref.setTitle(R.string.openDefaultPrompt);
        openDefaultTabPref.setSummary(R.string.openDefaultPromptExtra);
        openDefaultTabPref.setEntries(R.array.openDefaultTabOptions);
        openDefaultTabPref.setEntryValues(EpisodeType.getEpisodeListingTypeArray());
        openDefaultTabPref.setDefaultValue("0");
        root.addPreference(openDefaultTabPref);
        
        ListPreference openThemePref = new ListPreference(this);
        openThemePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				refreshDialog = true;
				return true;
			}
        });
        openThemePref.setKey(PreferencesKeys.THEME_KEY);
        openThemePref.setTitle(R.string.ThemePrompt);
        openThemePref.setSummary(R.string.ThemePromptExtra);
        openThemePref.setEntries(R.array.ThemeOptions);
        openThemePref.setEntryValues(R.array.ThemeValues);
        openThemePref.setDefaultValue("0");
        root.addPreference(openThemePref);
        
        ListPreference showLanguagePref = new ListPreference(this);
        showLanguagePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				refreshDialog = true;
				return true;
			}
        });
        showLanguagePref.setKey(PreferencesKeys.LANGUAGE_KEY);
        showLanguagePref.setTitle(R.string.showLanguagePrompt);
        showLanguagePref.setEntries(R.array.showLanguageOptions);
        showLanguagePref.setEntryValues(R.array.showLanguageOptionsValues);
        root.addPreference(showLanguagePref);

        return root;
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case RELAOD_DIALOG:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.attention)
					   .setMessage(R.string.applyPreferences)
					   .setCancelable(false)
					   .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				                startTabMain();
				           }
				       });
				dialog = builder.create();
				break;
			default:
				dialog = super.onCreateDialog(id);
				break;
		}
		return dialog;
    }

    @Override
    public void finish() {
        Log.d("PreferencesActivity", "Closing the preferences screen!");
        
        if (refreshDialog)
        {
        	showDialog(RELAOD_DIALOG);
        }
        else
        {
        	super.finish();
        }
    }
    
    private void startTabMain() {
    	super.finish();
    	Intent mainActivity = new Intent(this.getApplicationContext(), EpisodeListingActivity.class);
    	mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
    	startActivity(mainActivity);
    }
}
