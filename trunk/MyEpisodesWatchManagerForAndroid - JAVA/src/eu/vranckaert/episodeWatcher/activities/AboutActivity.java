package eu.vranckaert.episodeWatcher.activities;

import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.utils.ApplicationUtil;
import roboguice.activity.GuiceActivity;

public class AboutActivity extends GuiceActivity {
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
        setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        ((TextView) findViewById(R.id.title_text)).setText(R.string.about);
    }
    
    public void onHomeClick(View v) {
    	finish();
    }
}
