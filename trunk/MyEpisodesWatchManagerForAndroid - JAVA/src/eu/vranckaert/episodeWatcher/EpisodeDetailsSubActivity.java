package eu.vranckaert.episodeWatcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.vranckaert.episodeWatcher.domain.Episode;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EpisodeDetailsSubActivity extends Activity {
	Episode episode = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.episodedetails);
        
        Bundle data = this.getIntent().getExtras();
        
        TextView showNameText = (TextView) findViewById(R.id.episodeDetShowName);
        TextView episodeNameText = (TextView) findViewById(R.id.episodeDetName);
        TextView seasonText = (TextView) findViewById(R.id.episodeDetSeason);
        TextView episodeText = (TextView) findViewById(R.id.episodeDetEpisode);
        TextView airdateText = (TextView) findViewById(R.id.episodeDetAirdate);
        
        episode = (Episode) data.getSerializable("episode");
        
        showNameText.setText(episode.getShowName());
        episodeNameText.setText(episode.getName());
        seasonText.setText(" " + episode.getSeason());
        episodeText.setText(" " + episode.getEpisode());
        
        //Air date in specifc format
        Date airdate = episode.getAirDate();
        if (airdate == null) {
        	airdate = new Date();
        }
        DateFormat formatter = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
        String formattedAirDate = formatter.format(airdate);
        
        airdateText.setText(" " + formattedAirDate);
        
        Button markAsSeenButton = (Button) findViewById(R.id.markAsSeenButton);
        
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
		inflater.inflate(R.menu.episodedetailsmenu, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.markAsSeen:
			closeAndMarkWatched(episode);
			return true;
		}
		return false;
	}
    
    private void closeAndMarkWatched(Episode episode) {
    	Intent intent = new Intent();
    	intent.putExtra("markEpisodeWatched", true);
    	intent.putExtra("episode", episode);
    	
    	setResult(RESULT_OK, intent);
		finish();
	}
}
