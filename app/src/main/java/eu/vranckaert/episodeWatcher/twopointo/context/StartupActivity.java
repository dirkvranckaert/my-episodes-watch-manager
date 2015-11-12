package eu.vranckaert.episodeWatcher.twopointo.context;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

/**
 * Date: 03/11/15
 * Time: 08:03
 *
 * @author Dirk Vranckaert
 */
public class StartupActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isLoggedIn()) {
            NavigationManager.startApp(this);
        } else {
            NavigationManager.startLogon(this);
        }
        finish();
    }

    private boolean isLoggedIn() {
        if (!Preferences.getPreferenceBoolean(this, PreferencesKeys.STORE_PASSWORD_KEY, true)) {
            Preferences.removePreference(this, User.PASSWORD);
        }

        String username = Preferences.getPreference(this, User.USERNAME);
        String password = Preferences.getPreference(this, User.PASSWORD);

        if (username == null || password == null) {
            return false;
        } else {
            return true;
        }
    }
}
