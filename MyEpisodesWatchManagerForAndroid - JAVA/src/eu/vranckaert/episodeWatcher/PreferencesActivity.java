package eu.vranckaert.episodeWatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

/**
 * @author Dirk Vranckaert
 *         Date: 13-mei-2010
 *         Time: 19:34:14
 */
public class PreferencesActivity extends PreferenceActivity {
    private static final int RELAOD_DIALOG = 0;

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

    private void exit() {
        super.finish();
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        
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
        showOrderingPref.setKey(PreferencesKeys.SHOW_SORTING_KEY);
        showOrderingPref.setTitle(R.string.showOrderPrompt);
        showOrderingPref.setEntries(R.array.showOrderOptions);
        showOrderingPref.setEntryValues(R.array.showOrderOptionsValues);
        root.addPreference(showOrderingPref);

        ListPreference episodeOrderingPref = new ListPreference(this);
        episodeOrderingPref.setKey(PreferencesKeys.EPISODE_SORTING_KEY);
        episodeOrderingPref.setTitle(R.string.episodeOrderPrompt);
        episodeOrderingPref.setSummary(R.string.episodeOrderPromptExtra);
        episodeOrderingPref.setEntries(R.array.episodeOrderOptions);
        episodeOrderingPref.setEntryValues(R.array.episodeOrderOptionsValues);
        root.addPreference(episodeOrderingPref);

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
			default:
				dialog = super.onCreateDialog(id);
				break;
		}
		return dialog;
    }

    @Override
    public void finish() {
        Log.d("PreferencesActivity", "Closing the preferences screen!");
        showDialog(RELAOD_DIALOG);
    }
}
