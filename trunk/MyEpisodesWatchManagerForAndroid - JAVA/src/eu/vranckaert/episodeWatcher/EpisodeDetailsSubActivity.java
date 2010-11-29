package eu.vranckaert.episodeWatcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.utils.DateUtil;

import java.util.Date;

public class EpisodeDetailsSubActivity extends Activity {
	Episode episode = null;
	int episodesType;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.episodedetails);
        Bundle data = this.getIntent().getExtras();
        
        TextView showNameText = (TextView) findViewById(R.id.episodeDetShowName);
        TextView episodeNameText = (TextView) findViewById(R.id.episodeDetName);
        TextView seasonText = (TextView) findViewById(R.id.episodeDetSeason);
        TextView episodeText = (TextView) findViewById(R.id.episodeDetEpisode);
        TextView airdateText = (TextView) findViewById(R.id.episodeDetAirdate);
        
        episode = (Episode) data.getSerializable("episode");
        episodesType = (Integer) data.getSerializable("episodesType");
        
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
        ImageButton twitterButton = (ImageButton) findViewById(R.id.twitterButton);
        
        if (episodesType != 0)
        {
        	twitterButton.setVisibility(View.GONE);
        }
        if (episodesType != 1)
        {
        	markAsAcquiredButton.setVisibility(View.GONE);
        }
        if (episodesType == 2)
        {
        	markAsSeenButton.setVisibility(View.GONE);
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
        
        twitterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tweetThis();
			}
		});
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episodedetailsmenu, menu);
		if (episodesType != 1)
		{
			menu.removeItem(R.id.markAsAquired);
		}
		if (episodesType == 2)
		{
			menu.removeItem(R.id.markAsSeen);
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
    	Intent intent = new Intent();
    	intent.putExtra("markEpisode", "acquire");
    	intent.putExtra("episode", episode);
    	
    	setResult(RESULT_OK, intent);
		finish();
	}
    
    private void tweetThis() {
    	String tweet = episode.getShowName() + " S" + episode.getSeasonString() + "E" + episode.getEpisodeString();
    	Intent i = new Intent(android.content.Intent.ACTION_SEND);
    	i.setType("text/plain");
    	i.putExtra(Intent.EXTRA_TEXT, getString(R.string.Tweet, tweet));
    	startActivity(Intent.createChooser(i, getString(R.string.TweetTitle)));
    }
    
    private void closeAndMarkWatched(Episode episode) {
    	Intent intent = new Intent();
    	intent.putExtra("markEpisode", "watch");
    	intent.putExtra("episode", episode);
    	
    	setResult(RESULT_OK, intent);
		finish();
	}
}
