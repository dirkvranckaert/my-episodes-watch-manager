package eu.vranckaert.episodeWatcher.activities;

import java.util.Date;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.constants.ActivityConstants;
import eu.vranckaert.episodeWatcher.controllers.EpisodesController;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.enums.ListMode;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.utils.DateUtil;
import roboguice.activity.GuiceActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class RandomEpPickerActivity extends GuiceActivity {
	private Episode random;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.randompicker);
        
        TextView showNameText = (TextView) findViewById(R.id.episodeDetShowName);
        TextView episodeNameText = (TextView) findViewById(R.id.episodeDetName);
        TextView seasonText = (TextView) findViewById(R.id.episodeDetSeason);
        TextView episodeText = (TextView) findViewById(R.id.episodeDetEpisode);
        TextView airdateText = (TextView) findViewById(R.id.episodeDetAirdate);
        
        ((TextView) findViewById(R.id.title_text)).setText(R.string.randompicker);
        Button markAsSeenButton = (Button) findViewById(R.id.markAsSeenButton);
        
        if (EpisodesController.getInstance().getEpisodesCount(EpisodeType.EPISODES_TO_WATCH) > 0) {
	        random = EpisodesController.getInstance().getRandomWatchEpisode();
	        
	        showNameText.setText(random.getShowName());
	        episodeNameText.setText(random.getName());
	        seasonText.setText(" " + random.getSeasonString());
	        episodeText.setText(" " + random.getEpisodeString());
	        
	        //Air date in specifc format
	        Date airdate = random.getAirDate();
	        String formattedAirDate = null;
	        if (airdate != null) {
	        	formattedAirDate = DateUtil.formatDateLong(airdate, this);
	        } else {
	            formattedAirDate = getText(R.string.episodeDetailsAirDateLabelDateNotFound).toString();
	        }
	        
	        airdateText.setText(" " + formattedAirDate);
	        
	        markAsSeenButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					closeAndMarkWatched(random);
				}
			});
        } else {
        	((TextView) findViewById(R.id.randomtexttitle)).setText(getString(R.string.watchListSubTitleWatchPlural, 0));
        	((ImageButton) findViewById(R.id.btn_title_refresh)).setVisibility(View.GONE);
	        seasonText.setText("-");
	        episodeText.setText("-");
	        airdateText.setText("-");
	        
	        markAsSeenButton.setVisibility(View.GONE);
        }
    }
    
    private void closeAndMarkWatched(Episode episode) {
    	finish();
    	
		Intent episodeListingActivity = new Intent(this.getApplicationContext(), EpisodeListingActivity.class);
		episodeListingActivity.putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE, episode)
							  .putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_MARK_EPISODE, ActivityConstants.EXTRA_BUNDLE_VALUE_WATCH)
						      .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, episode.getType())
							  .putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_SHOW);
        startActivity(episodeListingActivity);
	}
	
    private void tweetThis() {
    	String tweet = random.getShowName() + " S" + random.getSeasonString() + "E" + random.getEpisodeString() + " - " + random.getName();
    	Intent i = new Intent(android.content.Intent.ACTION_SEND);
    	i.setType("text/plain");
    	i.putExtra(Intent.EXTRA_TEXT, getString(R.string.Tweet, tweet));
    	startActivity(Intent.createChooser(i, getString(R.string.TweetTitle)));
    }
	
    public void onHomeClick(View v) {
    	exit();
    }
    
    private void exit() {
    	finish();
	}

	public void onTweetClick(View v) {
    	tweetThis();
    }
}
