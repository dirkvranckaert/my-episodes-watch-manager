package be.dvwd.episodeWatcher;

import java.util.ArrayList;
import java.util.List;

import be.dvwd.episodeWatcher.domain.Episode;
import be.dvwd.episodeWatcher.domain.User;
import be.dvwd.episodeWatcher.exception.UnableToReadFeed;
import be.dvwd.episodeWatcher.service.MyEpisodesService;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
		this.user = new User("dirken", "9632dirk");
		this.myEpisodesService = new MyEpisodesService();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.watchlist);
        episodes = new ArrayList<Episode>();
        episodeAdapter = new EpisodeAdapter(this, R.layout.episode_row, episodes);
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
				EpisodesWatchListActivity.this,
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
		private LayoutInflater inflater;
		private int resource;
		
		public EpisodeAdapter(Context context, int textViewResourceId, List<Episode> el) {
			super(context, textViewResourceId, el);
			this.episodes = el;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			resource = textViewResourceId;
		}
		
		@Override
		public View getView(int posistion, View convertView, ViewGroup parent) {
			View view;
			if (convertView==null) {
				view = inflater.inflate(resource, parent, false);
			} else {
				view = convertView;
			}
			
			Episode episode = episodes.get(posistion);
			if (episode != null) {
				TextView topText = (TextView) findViewById(R.id.episodeRowTitle);
				TextView bottomText = (TextView) findViewById(R.id.episodeRowDetail);
				if (topText != null && bottomText != null) {
					topText.setText(episode.getShowName());
					bottomText.setText("S" + episode.getSeason() + "E" + episode.getEpisode() + " - " + episode.getName());
				}
			}
			
			return view;
		}
	}
}
