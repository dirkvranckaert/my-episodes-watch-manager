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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class EpisodesWatchListActivity extends ListActivity {
	private User user;
    private MyEpisodesService myEpisodesService;
    private List<Episode> episodes = new ArrayList<Episode>(0);
    private Episode currentEpisode= null;
    private TextView subTitle;
    private EpisodeAdapter episodeAdapter;
    private ProgressDialog progressDialog;
    private Runnable viewEpisodes;
    private Runnable markEpisode;
	
	public EpisodesWatchListActivity() {
		super();
		//TODO: pass credentials from login activity to here instead of hardcoding
		this.user = new User("myUsername", "myPassword");
		this.myEpisodesService = new MyEpisodesService();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.watchlistmenu, menu);
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episodemenu, menu);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        init();
        
        openLoginActivity();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.refresh:
			reloadEpisodes();
			return true;
		case R.id.logout:
			logout();
			return true;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		currentEpisode = episodes.get(menuInfo.position);
		switch(item.getItemId()) {
		case R.id.episodeMenuWatched:
			markEpisodeWatched(currentEpisode);
			return true;
		}
		return false;
	}

	private void init() {
		setContentView(R.layout.watchlist);
        episodes = new ArrayList<Episode>();
        episodeAdapter = new EpisodeAdapter(this, R.layout.episoderow, episodes);
        setListAdapter(episodeAdapter);
        registerForContextMenu(getListView());
	}

	private void openLoginActivity() {
		Intent loginSubActivity = new Intent(this.getApplicationContext(), LoginSubActivity.class);
        startActivityForResult(loginSubActivity, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
	        user = new User(
        		Preferences.getPreference(this, User.USERNAME),
        		Preferences.getPreference(this, User.PASSWORD)
    		);
	        
	        reloadEpisodes();
		} else {
			exit();
		}
	}
	
	private void reloadEpisodes() {
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
			Toast.makeText(EpisodesWatchListActivity.this, R.string.watchListUnableToReadFeed, Toast.LENGTH_LONG);
			e.printStackTrace();
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
	
	private void markEpisodeWatched(final Episode episode) {
		markEpisode = new Runnable() {
			@Override
			public void run() {
				markEpisode(episode);
			}
		};
        
		Thread thread =  new Thread(null, markEpisode, "EpisodeMarkWatchedBackground");
		thread.start();
		
		progressDialog = ProgressDialog.show(
				this,
				getString(R.string.progressLoadingTitle),
				getString(R.string.progressLoadingBody),
				true
		);
	}
	
	private void markEpisode(Episode episode) {
		try {
			myEpisodesService.watchedEpisode(episode, user);
		} catch (LoginFailedException e) {
			Toast.makeText(EpisodesWatchListActivity.this, R.string.watchListUnableToMarkWatched, Toast.LENGTH_LONG);
			e.printStackTrace();
		} catch (ShowUpdateFailedException e) {
			Toast.makeText(EpisodesWatchListActivity.this, R.string.watchListUnableToMarkWatched, Toast.LENGTH_LONG);
			e.printStackTrace();
		}
		runOnUiThread(delegateEpisodeReloading);
	}
	
	private Runnable delegateEpisodeReloading = new Runnable() {
		@Override
		public void run() {
			progressDialog.dismiss();
			reloadEpisodes();
		}
	};
	
	private void logout() {
		Preferences.removePreference(this, User.USERNAME);
		Preferences.removePreference(this, User.PASSWORD);
		openLoginActivity();
	}
	
	private void exit() {
		finish();
	}
}