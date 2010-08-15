package eu.vranckaert.episodeWatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

/**
 * @author Dirk Vranckaert
 *         Date: 13-mei-2010
 *         Time: 19:34:14
 */
public class PreferencesActivity extends PreferenceActivity {
    private static final int RELAOD_DIALOG = 0;
    private static final int LANGUAGE_DIALOG = RELAOD_DIALOG + 1;
    private boolean refreshDialog;
    private boolean languageDialog;

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
        super.onCreate(savedInstance);
        
        refreshDialog = false;
        languageDialog = false;
        
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
        
        ListPreference showLanguagePref = new ListPreference(this);
        showLanguagePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				languageDialog = true;
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
                                exit();
				           }
				       });
				dialog = builder.create();
				break;
			case LANGUAGE_DIALOG:
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setTitle(R.string.attention)
					   .setMessage(R.string.applyLanguagePreferences)
					   .setCancelable(false)
					   .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				                exit();
				           }
				       });
				dialog = builder2.create();
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
        if (languageDialog)
        {
        	showDialog(LANGUAGE_DIALOG);
        }
        else if (refreshDialog)
        {
        	showDialog(RELAOD_DIALOG);
        }
        else
        {
        	exit();
        }
    }
    
    private void exit() {
    	super.finish();
    }
}
