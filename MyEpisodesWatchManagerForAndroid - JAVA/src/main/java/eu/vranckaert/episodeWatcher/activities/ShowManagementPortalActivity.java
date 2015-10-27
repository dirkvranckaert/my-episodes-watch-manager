package eu.vranckaert.episodeWatcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import roboguice.activity.GuiceActivity;

/**
 * @author Ivo Janssen
 */
public class ShowManagementPortalActivity extends GuiceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	init(savedInstanceState);
        loadButtons();
    }

    private void init(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.show_management_portal);
        ((TextView) findViewById(R.id.title_text)).setText(R.string.manageShows);
    }

    private void loadButtons() {
        Button favoShowsButton = (Button) findViewById(R.id.selectionPanelFavoShows);
        Button ignoredShowsButton = (Button) findViewById(R.id.selectionPanelIgnoredShows);
        Button addShowsButton = (Button) findViewById(R.id.selectionPanelAddShows);
        favoShowsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFavouriteOrIgnoredShows(ShowType.FAVOURITE_SHOWS);
            }
        });
        ignoredShowsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFavouriteOrIgnoredShows(ShowType.IGNORED_SHOWS);
            }
        });
        addShowsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchActivity();
            }
        });
    }

    private void openSearchActivity() {
        Intent searchIntent = new Intent(this.getApplicationContext(), ShowManagementAddActivity.class);
        startActivity(searchIntent);
    }

    private void openFavouriteOrIgnoredShows(ShowType showType) {
        Intent intent = new Intent(this.getApplicationContext(), ShowManagementActivity.class);
        intent.putExtra(ShowType.class.getSimpleName(), showType);
        startActivity(intent);
    }
    
    public void onHomeClick(View v) {
    	finish();
    }
}
