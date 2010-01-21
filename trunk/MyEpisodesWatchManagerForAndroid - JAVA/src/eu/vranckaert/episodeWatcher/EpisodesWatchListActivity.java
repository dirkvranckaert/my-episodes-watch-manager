package eu.vranckaert.episodeWatcher;

import java.util.ArrayList;
import java.util.List;

import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.FeedUrlParsingException;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowUpdateFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;

import eu.vranckaert.episodeWatcher.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class EpisodesWatchListActivity extends ListActivity {
	private static final int LOGIN_REQUEST_CODE = 0;
	private static final int EPISODE_DETAILS_REQUEST_CODE = 1;
	
	private static final int EPISODE_LOADING_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 1;
	private static final String LOG_TAG = "EpisodeWatchListActivity";
	
	private User user;
    private MyEpisodesService myEpisodesService;
    private List<Episode> episodes = new ArrayList<Episode>(0);
    private TextView subTitle;
    private EpisodeAdapter episodeAdapter;
    private Runnable viewEpisodes;
    private Runnable markEpisode;
    
    private Integer exceptionMessageResId = null;
	
	public EpisodesWatchListActivity() {
		super();
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
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case EPISODE_LOADING_DIALOG:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.progressLoadingTitle));
				//progressDialog.setTitle(R.string.progressLoadingTitle);
				dialog = progressDialog;
				break;
			case EXCEPTION_DIALOG:
				if (exceptionMessageResId == null) {
					exceptionMessageResId = R.string.defaultExceptionMessage;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.exceptionDialogTitle)
					   .setMessage(exceptionMessageResId)
					   .setCancelable(false)
					   .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				dialog = builder.create();
				break;
			default:
				dialog = super.onCreateDialog(id);
				break;
		}
		return dialog;
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
		Episode currentEpisode = episodes.get(menuInfo.position);
		switch(item.getItemId()) {
		case R.id.episodeMenuWatched:
			markEpisodeWatched(currentEpisode);
			return true;
		case R.id.episodeMenuDetails:
			openEpisodeDetails(currentEpisode);
			return true;
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Episode currentEpisode = episodes.get(position);
		openEpisodeDetails(currentEpisode);
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
        startActivityForResult(loginSubActivity, LOGIN_REQUEST_CODE);
	}

	private void openEpisodeDetails(Episode episode) {
		Intent episodeDetailsSubActivity = new Intent(this.getApplicationContext(), EpisodeDetailsSubActivity.class);
		episodeDetailsSubActivity.putExtra("episode", episode);
        startActivityForResult(episodeDetailsSubActivity, EPISODE_DETAILS_REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
	        user = new User(
        		Preferences.getPreference(this, User.USERNAME),
        		Preferences.getPreference(this, User.PASSWORD)
    		);
	        
	        reloadEpisodes();
		} else if (requestCode == EPISODE_DETAILS_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bundle intentData = data.getExtras();
				boolean markEpisodeWathed = intentData.getBoolean("markEpisodeWatched");
				Episode episode = (Episode) intentData.getSerializable("episode");
				
				if (markEpisodeWathed) {
					markEpisode(episode);
				}
			}
		} else {
			exit();
		}
	}
	
	private void reloadEpisodes() {
		showDialog(EPISODE_LOADING_DIALOG);
		viewEpisodes = new Runnable() {
			@Override
			public void run() {
				getEpisodes();
			}
		};
        
		Thread thread =  new Thread(null, viewEpisodes, "EpisodeRetrievalBackground");
		thread.start();
	}
	
	private void getEpisodes() {
		try {
			episodes = myEpisodesService.retrieveEpisodes(user);
		} catch (InternetConnectivityException e) {
			String message = "Could not connect to host";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.internetConnectionFailureReload;
		} catch(FeedUrlParsingException e) { 
			String message = "Exception occured:";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.watchListUnableToReadFeed;
		} catch (Exception e) {
			String message = "Exception occured:";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.defaultExceptionMessage;
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
			} else {
				episodeAdapter.clear();
				subTitle = (TextView) findViewById(R.id.watchListSubTitle);
				subTitle.setText("");
			}
			dismissDialog(EPISODE_LOADING_DIALOG);
			
			if (exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
				showDialog(EXCEPTION_DIALOG);
				exceptionMessageResId = null;
			}
			
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
		showDialog(EPISODE_LOADING_DIALOG);
		markEpisode = new Runnable() {
			@Override
			public void run() {
				markEpisode(episode);
			}
		};
        
		Thread thread =  new Thread(null, markEpisode, "EpisodeMarkWatchedBackground");
		thread.start();
	}
	
	private void markEpisode(Episode episode) {
		try {
			myEpisodesService.watchedEpisode(episode, user);
		} catch (InternetConnectivityException e) {
			String message = "Could not connect to host";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.networkIssues;
		} catch (LoginFailedException e) {
			String message = "Login failure";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.networkIssues;
		} catch (ShowUpdateFailedException e) {
			String message = "Marking the show watched failed (" + episode + ")";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.watchListUnableToMarkWatched;
		} catch (UnsupportedHttpPostEncodingException e) {
			String message = "Network issues";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.networkIssues;
		} catch (Exception e) {
			String message = "Unknown exception occured";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.defaultExceptionMessage;
		}
		runOnUiThread(delegateEpisodeReloading);
	}
	
	private Runnable delegateEpisodeReloading = new Runnable() {
		@Override
		public void run() {
			dismissDialog(EPISODE_LOADING_DIALOG);
			
			if (exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
				showDialog(EXCEPTION_DIALOG);
				exceptionMessageResId = null;
			} else {			
				reloadEpisodes();
			}
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