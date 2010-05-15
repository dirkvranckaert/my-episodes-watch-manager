package eu.vranckaert.episodeWatcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import eu.vranckaert.episodeWatcher.preferences.enums.EpisodeSortingEnum;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.preferences.enums.ShowSortingEnum;

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
        handleOrderOfShowsPreference();
        handleOrderOfEpisodesPerference();
        handleStorePasswordPreference();
    }

    private void handleOrderOfShowsPreference() {
        final Activity ac = this;
        final Spinner spinner = (Spinner)findViewById(R.id.showOrderOptions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.showOrderOptions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        determineSelectedShowSortingOption(spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                ShowSortingEnum selectedSorting = ShowSortingEnum.ASCENDING;
                if (i == 0) {
                    selectedSorting = ShowSortingEnum.DEFAULT_MYEPISODES_COM;
                } else if (i == 1) {
                    selectedSorting = ShowSortingEnum.ASCENDING;
                } else if (i == 2) {
                    selectedSorting = ShowSortingEnum.DESCENDING;
                }

                Preferences.setPreference(ac, PreferencesKeys.SHOW_SORTING_KEY, selectedSorting.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing!
            }
        });
    }

    private void handleOrderOfEpisodesPerference() {
        final Activity ac = this;
        final Spinner spinner = (Spinner)findViewById(R.id.episodeOrderOptions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.episodeOrderOptions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        determineSelectedEpisodeSortingOption(spinner);

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

    private void determineSelectedShowSortingOption(Spinner spinner) {
        String orderPref = Preferences.getPreference(this, PreferencesKeys.SHOW_SORTING_KEY);
        if (orderPref.equals(ShowSortingEnum.DEFAULT_MYEPISODES_COM.getName())) {
            spinner.setSelection(0);
        } else if (orderPref.equals(ShowSortingEnum.ASCENDING.getName())) {
            spinner.setSelection(1);
        } else if (orderPref.equals(ShowSortingEnum.DESCENDING.getName())) {
            spinner.setSelection(2);
        }
    }

    private void determineSelectedEpisodeSortingOption(Spinner spinner) {
        String orderPref = Preferences.getPreference(this, PreferencesKeys.EPISODE_SORTING_KEY);
        if (orderPref.equals(EpisodeSortingEnum.NEWEST.getName())) {
            spinner.setSelection(1);
        } else {
            spinner.setSelection(0);
        }
    }

    private void handleStorePasswordPreference() {
        final Activity ac = this;

        CheckBox checkBox = (CheckBox) findViewById(R.id.showPasswordOption);
        checkBox.setChecked(Preferences.getPreferenceBoolean(this, PreferencesKeys.STORE_PASSWORD_KEY));

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Preferences.setPreference(ac, PreferencesKeys.STORE_PASSWORD_KEY, b);
            }
        });
    }
}
