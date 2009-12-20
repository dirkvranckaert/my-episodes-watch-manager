package eu.vranckaert.episodeWatcher;

import java.util.ArrayList;
import java.util.List;

import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowUpdateFailedException;
import eu.vranckaert.episodeWatcher.exception.UnableToReadFeed;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;

import eu.vranckaert.episodeWatcher.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EpisodesWatchListActivity extends ListActivity {
	private User user;
    private MyEpisodesService myEpisodesService;
    private List<Episode> episodes = new ArrayList<Episode>(0);
    private Episode currentEpisode = null;
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
        
        setContentView(R.layout.watchlist);
        episodes = new ArrayList<Episode>();
        episodeAdapter = new EpisodeAdapter(this, R.layout.episoderow, episodes);
        setListAdapter(episodeAdapter);
        
        openLoginActivity();
	}
	
	public void reloadEpisodes() {
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
			ImageView markWatchedButton = (ImageView) row.findViewById(R.id.episodeMarkWatched);
			
			Episode episode = episodes.get(posistion);
			topText.setText(episode.getShowName());
			bottomText.setText("S" + episode.getSeason() + "E" + episode.getEpisode() + " - " + episode.getName());
			currentEpisode = episode;
			markWatchedButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						myEpisodesService.watchedEpisode(currentEpisode, user);
					} catch (LoginFailedException e) {
						Toast.makeText(EpisodesWatchListActivity.this, R.string.markWatchFailedException, Toast.LENGTH_LONG).show();
					} catch (ShowUpdateFailedException e) {
						// TODO Auto-generated catch block
						Toast.makeText(EpisodesWatchListActivity.this, R.string.markWatchFailedException, Toast.LENGTH_LONG).show();
					}
					reloadEpisodes();
				}
			});
			
			return row;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle userBundle = data.getExtras();
	        user = new User(
        		userBundle.getString(User.USERNAME),
        		userBundle.getString(User.PASSWORD)
    		);
	        
	        reloadEpisodes();
		} else {
			finish();
		}
	}

	private void openLoginActivity() {
		Intent loginSubActivity = new Intent(this.getApplicationContext(), LoginSubActivity.class);
        startActivityForResult(loginSubActivity, 0);
	}
}