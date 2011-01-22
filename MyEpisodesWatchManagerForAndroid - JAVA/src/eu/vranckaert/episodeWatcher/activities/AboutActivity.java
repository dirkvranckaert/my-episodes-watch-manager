package eu.vranckaert.episodeWatcher.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.utils.ApplicationUtil;

public class AboutActivity extends Activity {
    private static final String LOG_TAG = AboutActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init(savedInstanceState);

        //Application version
        String version = ApplicationUtil.getCurrentApplicationVersion(this);

        Log.d(LOG_TAG, "Current version of the application: " + version);

        TextView textVersion = (TextView) findViewById(R.id.aboutVersion);
        textVersion.setText(version);

        TextView aboutEmail = (TextView) findViewById(R.id.aboutEmail);
        Linkify.addLinks(aboutEmail, Linkify.EMAIL_ADDRESSES);

        TextView aboutWebsite = (TextView) findViewById(R.id.aboutWebsite);
        Linkify.addLinks(aboutWebsite, Linkify.WEB_URLS);
    }

    private void init(Bundle savedInstanceState) {
        setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
}
