package eu.vranckaert.episodeWatcher;

import java.util.Date;

import eu.vranckaert.episodeWatcher.domain.Episode;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class EpisodeDetailsSubActivity extends Activity {
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
        
        Episode episode = new Episode();
        //episode.setAirDate(new Date(data.getString("episode:airDate")));
        episode.setEpisode(data.getInt("episode:episode"));
        episode.setMyEpisodeID(data.getString("episode:myEpisodeID"));
        episode.setName(data.getString("episode:name"));
        episode.setSeason(data.getInt("episode:season"));
        episode.setShowName(data.getString("episode:showName"));
        
        showNameText.setText(episode.getShowName());
        episodeNameText.setText(episode.getName());
        //seasonText.setText(episode.getSeason());
        //episodeText.setText(episode.getEpisode());
        //airdateText.setText(episode.getAirDate().toLocaleString());
    }
}
