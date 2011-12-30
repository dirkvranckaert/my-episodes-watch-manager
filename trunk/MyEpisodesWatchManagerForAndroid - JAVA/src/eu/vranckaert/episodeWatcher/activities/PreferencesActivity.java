package eu.vranckaert.episodeWatcher.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import roboguice.activity.GuicePreferenceActivity;

/**
 * @author Ivo Janssen
 */
public class PreferencesActivity extends GuicePreferenceActivity {
    private static final int RELAOD_DIALOG = 0;
    private boolean refreshDialog;
    private EditTextPreference daysBackCP;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preferences_menu, menu);
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
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
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
        
        final CheckBoxPreference daysBackwardEnable = new CheckBoxPreference(this);
        daysBackwardEnable.setDefaultValue(false);      
        daysBackwardEnable.setKey(PreferencesKeys.DAYS_BACKWARD_ENABLED_KEY);
        daysBackwardEnable.setTitle(R.string.daysBackwardEnable);
        daysBackwardEnable.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
        @Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
				refreshDialog = true;
				if (daysBackwardEnable.isChecked()) {
					daysBackCP.setEnabled(false);
				} else {
					daysBackCP.setEnabled(true);
				}
				return true;
			}
        });
        root.addPreference(daysBackwardEnable);
        
        daysBackCP = new EditTextPreference(this);
        daysBackCP.setTitle(R.string.daysBackwardCP);
        daysBackCP.setKey(PreferencesKeys.DAYS_BACKWARDCP);
        daysBackCP.setSummary(R.string.daysBackwardCPExtra);
        daysBackCP.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        
        daysBackCP.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
            @Override
    		public boolean onPreferenceChange(Preference preference, Object newValue) {
    			refreshDialog = true;
    			return true;    	    	
    		}
        });
        
        if (!daysBackwardEnable.isChecked()) {
        	daysBackCP.setEnabled(false);
        }
        
        root.addPreference(daysBackCP);

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

        ListPreference openAcquirePref = new ListPreference(this);
        openAcquirePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				refreshDialog = true;
				return true;
			}
        });
        openAcquirePref.setKey(PreferencesKeys.ACQUIRE_KEY);
        openAcquirePref.setTitle(R.string.openAcquirePrompt);
        openAcquirePref.setSummary(R.string.openAcquirePromptExtra);
        openAcquirePref.setEntries(R.array.openAcquireOptions);
        openAcquirePref.setEntryValues(R.array.AcquireValues);
        root.addPreference(openAcquirePref);
        
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
    	setResult(RESULT_OK);
    	super.finish();
    }
}