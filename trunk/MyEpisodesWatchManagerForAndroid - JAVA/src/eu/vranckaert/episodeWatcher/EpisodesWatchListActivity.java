package eu.vranckaert.episodeWatcher;

import java.util.ArrayList;
import java.util.List;

import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.UnableToReadFeed;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;

import eu.vranckaert.episodeWatcher.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EpisodesWatchListActivity extends ListActivity {
	private User user;
    private MyEpisodesService myEpisodesService;
    private List<Episode> episodes = new ArrayList<Episode>(0);
    private TextView subTitle;
    private EpisodeAdapter episodeAdapter;
    private ProgressDialog progressDialog;
    private Runnable viewEpisodes;
	
	public EpisodesWatchListActivity() {
		super();
		//TODO: pass credentials from login activity to here instead of hardcoding
		this.user = new User("myUsername", "myPassword");
		this.myEpisodesService = new MyEpisodesService();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		Bundle userBundle = getIntent().getExtras();
        user = new User(userBundle.getString(User.USERNAME), userBundle.getString(User.PASSWORD));
        
        setContentView(R.layout.watchlist);
        episodes = new ArrayList<Episode>();
        episodeAdapter = new EpisodeAdapter(this, R.layout.episoderow, episodes);
        setListAdapter(episodeAdapter);
        viewEpisodes = new Runnable() {
			@Override
			public void run() {
				getEpisodes();
			}
		};
        
		Thread thread =  new Thread(null, viewEpisodes, "EpisodeRetrievalBackground");
		thread.start();
		progressDialog = ProgressDialog.show(
				this,
				getString(R.string.progressLoadingTitle),
				getString(R.string.progressLoadingBody),
				true
		);
	}
	
	private void getEpisodes() {
		try {
			episodes = myEpisodesService.retrieveEpisodes(user);
		} catch (UnableToReadFeed e) {
			
		}
		runOnUiThread(returnEpisodes);
	}
	
	private Runnable returnEpisodes = new Runnable() {
		@Override
		public void run() {
			if (episodes != null && episodes.size() > 0) {
				episodeAdapter.notifyDataSetChanged();
				episodeAdapter.clear();
				for (Episode ep : episodes) {
					episodeAdapter.add(ep);
				}
				
				subTitle = (TextView) findViewById(R.id.watchListSubTitle);
		        subTitle.setText(getString(R.string.watchListSubTitle, episodes.size()));
			}
			progressDialog.dismiss();
			episodeAdapter.notifyDataSetChanged();
		}
	};
	
	private class EpisodeAdapter extends ArrayAdapter<Episode> {
		private List<Episode> episodes;
		
		public EpisodeAdapter(Context context, int textViewResourceId, List<Episode> el) {
			super(context, textViewResourceId, el);
			this.episodes = el;
		}
		
		@Override
		public View getView(int posistion, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row==null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.episoderow, parent, false);
			}
			
			TextView topText = (TextView) row.findViewById(R.id.episodeRowTitle);
			TextView bottomText = (TextView) row.findViewById(R.id.episodeRowDetail);
			
			Episode episode = episodes.get(posistion);
			topText.setText(episode.getShowName());
			bottomText.setText("S" + episode.getSeason() + "E" + episode.getEpisode() + " - " + episode.getName());
			
			return row;
		}
	}
}