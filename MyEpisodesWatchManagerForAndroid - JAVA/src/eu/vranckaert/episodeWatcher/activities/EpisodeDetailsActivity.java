package eu.vranckaert.episodeWatcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.constants.ActivityConstants;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.enums.ListMode;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.utils.DateUtil;
import roboguice.activity.GuiceActivity;

import java.util.Date;

/**
 * @author Ivo Janssen
 */
public class EpisodeDetailsActivity extends GuiceActivity {
	private Episode episode = null;
	private EpisodeType episodesType;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.episode_details);
        
        Bundle data = this.getIntent().getExtras();
        
        TextView showNameText = (TextView) findViewById(R.id.episodeDetShowName);
        TextView episodeNameText = (TextView) findViewById(R.id.episodeDetName);
        TextView seasonText = (TextView) findViewById(R.id.episodeDetSeason);
        TextView episodeText = (TextView) findViewById(R.id.episodeDetEpisode);
        TextView airdateText = (TextView) findViewById(R.id.episodeDetAirdate);
        
        ((TextView) findViewById(R.id.title_text)).setText(R.string.details);
        
        episode = (Episode) data.getSerializable(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE);
        episodesType = (EpisodeType) data.getSerializable(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE);
        
        showNameText.setText(episode.getShowName());
        episodeNameText.setText(episode.getName());
        seasonText.setText(" " + episode.getSeasonString());
        episodeText.setText(" " + episode.getEpisodeString());
        
        //Air date in specifc format
        Date airdate = episode.getAirDate();
        String formattedAirDate = null;
        if (airdate != null) {
        	formattedAirDate = DateUtil.formatDateLong(airdate, this);
        } else {
            formattedAirDate = getText(R.string.episodeDetailsAirDateLabelDateNotFound).toString();
        }
        
        airdateText.setText(" " + formattedAirDate);
        
        Button markAsAcquiredButton = (Button) findViewById(R.id.markAsAcquiredButton);
        Button markAsSeenButton = (Button) findViewById(R.id.markAsSeenButton);
        
        switch(episodesType) {
	        case EPISODES_TO_WATCH:
	        	markAsAcquiredButton.setVisibility(View.GONE);
	        	break;
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
	        case EPISODES_TO_ACQUIRE:
	        	break;
	        case EPISODES_COMING:
	        	markAsAcquiredButton.setVisibility(View.GONE);
	        	break;
        }
        
        markAsAcquiredButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeAndAcquireEpisode(episode);
			}
		});        
        
        markAsSeenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeAndMarkWatched(episode);
			}
		});
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episode_details_menu, menu);
		if (episodesType.equals(EpisodeType.EPISODES_TO_WATCH)) {
			menu.removeItem(R.id.markAsAquired);
		} else if (episodesType.equals(EpisodeType.EPISODES_COMING)) {
			menu.removeItem(R.id.markAsAquired);
		}
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.markAsSeen:
			closeAndMarkWatched(episode);
			return true;
		case R.id.markAsAquired:
			closeAndAcquireEpisode(episode);
			return true;
		}
		return false;
	}
    
    private void closeAndAcquireEpisode(Episode episode) {
    	finish();
    	
    	String[] showOrderOptions = getResources().getStringArray(R.array.showOrderOptionsValues);
    	
		Intent episodeListingActivity = new Intent(this.getApplicationContext(), EpisodeListingActivity.class);
		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE, episode)
							  .putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_MARK_EPISODE, ActivityConstants.EXTRA_BUNDLE_VALUE_AQUIRE)
						      .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, episodesType);
		
    	String sorting = ""; 
    	
        switch(episodesType) {
	        case EPISODES_TO_WATCH:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.WATCH_SHOW_SORTING_KEY);
	        	break;
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
	        case EPISODES_TO_ACQUIRE:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.ACQUIRE_SHOW_SORTING_KEY);
	        	break;
	        case EPISODES_COMING:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.COMING_SHOW_SORTING_KEY);
	        	break;
        }
    	
    	if (sorting.equals(showOrderOptions[3])) {
    		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_DATE);
    	} else {
    		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_SHOW);
    	}
		
        startActivity(episodeListingActivity);
	}
    
    private void tweetThis() {
    	String tweet = episode.getShowName() + " S" + episode.getSeasonString() + "E" + episode.getEpisodeString() + " - " + episode.getName();
    	Intent i = new Intent(android.content.Intent.ACTION_SEND);
    	i.setType("text/plain");
    	i.putExtra(Intent.EXTRA_TEXT, getString(R.string.Tweet, tweet));
    	startActivity(Intent.createChooser(i, getString(R.string.TweetTitle)));
    }
    
    private void closeAndMarkWatched(Episode episode) {
    	finish();
    	
    	String[] showOrderOptions = getResources().getStringArray(R.array.showOrderOptionsValues);
    	
		Intent episodeListingActivity = new Intent(this.getApplicationContext(), EpisodeListingActivity.class);
		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE, episode)
							  .putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_MARK_EPISODE, ActivityConstants.EXTRA_BUNDLE_VALUE_WATCH)
						      .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, episodesType);

    	String sorting = ""; 
    	
        switch(episodesType) {
	        case EPISODES_TO_WATCH:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.WATCH_SHOW_SORTING_KEY);
	        	break;
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
	        case EPISODES_TO_ACQUIRE:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.ACQUIRE_SHOW_SORTING_KEY);
	        	break;
	        case EPISODES_COMING:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.COMING_SHOW_SORTING_KEY);
	        	break;
        }
        
    	if (sorting.equals(showOrderOptions[3])) {
    		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_DATE);
    	} else {
    		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_SHOW);
    	}
		
        startActivity(episodeListingActivity);
	}
    
	@Override
	public final void onBackPressed() { exit(); }
    
    public void onHomeClick(View v) {
    	exit();
    }
    
    private void exit() {
    	finish();
    	
    	String[] showOrderOptions = getResources().getStringArray(R.array.showOrderOptionsValues);
    	
    	Intent episodeListingActivity = new Intent(this.getApplicationContext(), EpisodeListingActivity.class);
		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, episodesType);
    	
		String sorting = ""; 
    	
        switch(episodesType) {
	        case EPISODES_TO_WATCH:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.WATCH_SHOW_SORTING_KEY);
	        	break;
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
	        case EPISODES_TO_ACQUIRE:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.ACQUIRE_SHOW_SORTING_KEY);
	        	break;
	        case EPISODES_COMING:
	        	sorting = Preferences.getPreference(this, PreferencesKeys.COMING_SHOW_SORTING_KEY);
	        	break;
        }
        
    	if (sorting.equals(showOrderOptions[3])) {
    		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_DATE);
    	} else {
    		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_SHOW);
    	}
		
        startActivity(episodeListingActivity);
	}

	public void onTweetClick(View v) {
    	tweetThis();
    }
}
