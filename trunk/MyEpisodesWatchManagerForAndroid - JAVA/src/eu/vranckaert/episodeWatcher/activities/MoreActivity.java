package eu.vranckaert.episodeWatcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import roboguice.activity.GuiceActivity;

public class MoreActivity extends GuiceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
    }
    
    public void onHomeClick(View v) {
        finish();
    }
    
    public void onAboutClick(View v) {
        Intent manageShowsActivity = new Intent(this.getApplicationContext(), AboutActivity.class);
        startActivity(manageShowsActivity);
    }
    
    public void onTodoClick(View v) {
        Intent manageShowsActivity = new Intent(this.getApplicationContext(), ChangelogActivity.class);
        startActivity(manageShowsActivity);
    }
    
    public void onRandomClick(View v) {
    	finish();
    	
        Intent randomActivity = new Intent(this.getApplicationContext(), RandomEpPickerActivity.class);
        startActivity(randomActivity);
    }
}
