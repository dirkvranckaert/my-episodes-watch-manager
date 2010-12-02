package eu.vranckaert.episodeWatcher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

/**
 * @author Dirk Vranckaert
 *         Date: 18-sep-2010
 *         Time: 13:09:05
 */
public class ShowManagementActivity extends Activity {
    private static final String LOG_TAG = ShowManagementActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	init(savedInstanceState);
        loadButtons();
    }

    private void init(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.showmanagement);
    }

    private void loadButtons() {
        LinearLayout favoShowsButton = (LinearLayout) findViewById(R.id.selectionPanelFavoShows);
        LinearLayout ignoredShowsButton = (LinearLayout) findViewById(R.id.selectionPanelIgnoredShows);
        LinearLayout addShowsButton = (LinearLayout) findViewById(R.id.selectionPanelAddShows);
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
        Intent searchIntent = new Intent(this.getApplicationContext(), ShowSearchActivity.class);
        startActivity(searchIntent);
    }

    private void openFavouriteOrIgnoredShows(ShowType showType) {
        Intent intent = new Intent(this.getApplicationContext(), ShowFavosAndIngoredManagementActivity.class);
        intent.putExtra(ShowType.class.getSimpleName(), showType);
        startActivity(intent);
    }
}
