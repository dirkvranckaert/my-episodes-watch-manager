package eu.vranckaert.episodeWatcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;

/**
 * @author Dirk Vranckaert
 *         Date: 18-sep-2010
 *         Time: 13:09:05
 */
public class ManageActivity extends Activity {
    private static final String LOG_TAG = ManageActivity.class.getSimpleName();

    private MyEpisodesService myEpisodesService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	init(savedInstanceState);

        //TODO TEMP FOR TESTING
        User user = new User(
            Preferences.getPreference(this, User.USERNAME),
            Preferences.getPreference(this, User.PASSWORD)
        );
        try {
            myEpisodesService.getFavoriteOrIgnoredShows(user, ShowType.FAVORITE_SHOW);
        } catch (UnsupportedHttpPostEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InternetConnectivityException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (LoginFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void init(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.showmanagement);
        myEpisodesService = new MyEpisodesService();
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
