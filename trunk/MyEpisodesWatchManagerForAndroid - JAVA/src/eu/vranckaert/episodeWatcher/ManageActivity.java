package eu.vranckaert.episodeWatcher;

import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Dirk Vranckaert
 *         Date: 18-sep-2010
 *         Time: 13:09:05
 */
public class ManageActivity extends Activity {
    private static final String LOG_TAG = "ManageActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manageshows, menu);
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
            case R.id.searchShows:
                openSearchActivity();
                return true;
        }
        return false;
    }

    private void openSearchActivity() {
        Intent searchIntent = new Intent(this.getApplicationContext(), ShowSearchActivity.class);
        startActivity(searchIntent);
    }
}
