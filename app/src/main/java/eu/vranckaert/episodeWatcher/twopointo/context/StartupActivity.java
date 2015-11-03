package eu.vranckaert.episodeWatcher.twopointo.context;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.preferences.Preferences;

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
            Toast.makeText(this, "Already logged on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Should logon first", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private boolean isLoggedIn() {
        String username = Preferences.getPreference(this, User.USERNAME);
        String password = Preferences.getPreference(this, User.PASSWORD);

        if (username == null || password == null) {
            return false;
        } else {
            return true;
        }
    }
}
