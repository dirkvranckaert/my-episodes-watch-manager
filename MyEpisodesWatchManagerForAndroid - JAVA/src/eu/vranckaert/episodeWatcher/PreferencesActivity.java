package eu.vranckaert.episodeWatcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import eu.vranckaert.episodeWatcher.utils.EpisodeSortingEnum;
import eu.vranckaert.episodeWatcher.utils.Preferences;
import eu.vranckaert.episodeWatcher.utils.PreferencesKeys;

/**
 * @author Dirk Vranckaert
 *         Date: 13-mei-2010
 *         Time: 19:34:14
 */
public class PreferencesActivity extends Activity {
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
                exit();
                return true;
        }
		return false;
	}

    private void exit() {
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.preferences);

        //Create a specific method for each preference to set
        handleOrderOfEpisodesPerference();
    }

    private void handleOrderOfEpisodesPerference() {
        final Activity ac = this;
        final Spinner spinner = (Spinner)findViewById(R.id.episodeOrderOptions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.episodeOrderOptions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        determineSelectedOption(spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                EpisodeSortingEnum selectedSorting = EpisodeSortingEnum.OLDEST;
                if (i == 0) {
                    selectedSorting = EpisodeSortingEnum.OLDEST;
                } else if (i == 1) {
                    selectedSorting = EpisodeSortingEnum.NEWEST;
                }

                Preferences.setPreference(ac, PreferencesKeys.EPISODE_SORTING_KEY, selectedSorting.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing!
            }
        });
    }

    private void determineSelectedOption(Spinner spinner) {
        String orderPref = Preferences.getPreference(this, PreferencesKeys.EPISODE_SORTING_KEY);
        if (orderPref.equals(EpisodeSortingEnum.NEWEST.getName())) {
            spinner.setSelection(1);
        } else {
            spinner.setSelection(0);
        }
    }
}
