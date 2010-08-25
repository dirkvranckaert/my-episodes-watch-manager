package eu.vranckaert.episodeWatcher;

import java.util.*;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import eu.vranckaert.episodeWatcher.domain.*;
import eu.vranckaert.episodeWatcher.exception.FeedUrlParsingException;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowUpdateFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;

import eu.vranckaert.episodeWatcher.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

public class EpisodesWatchListActivity extends ExpandableListActivity {
	private static final int LOGIN_REQUEST_CODE = 0;
	private static final int EPISODE_DETAILS_REQUEST_CODE = 1;
	
	private static final int EPISODE_LOADING_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 1;
	private static final int ABOUT_DIALOG = 2;
	private static final int CHANGELOG_DIALOG = 3;
	private static final int LOGOUT_DIALOG = 4;
	private static final String LOG_TAG = "EpisodeWatchListActivity";
	
	private User user;
    private MyEpisodesService myEpisodesService;
    private List<Episode> episodes = new ArrayList<Episode>();
    private List<Show> shows = new ArrayList<Show>();
    private TextView Title;
    private TextView subTitle;
    private SimpleExpandableListAdapter episodeAdapter;
    private Runnable viewEpisodes;
    private Runnable markEpisode;
    private Integer exceptionMessageResId = null;
    private int episodesType;
	private Resources res; // Resource object to get Drawables
	private android.content.res.Configuration conf;
    
    private GoogleAnalyticsTracker tracker;
	
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupid = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childid = ExpandableListView.getPackedPositionChild(info.packedPosition);
			
			menu.setHeaderTitle(shows.get(groupid).getEpisodes().get(childid).getShowName() + 
					" S" + shows.get(groupid).getEpisodes().get(childid).getSeasonString() + 
					"E" + shows.get(groupid).getEpisodes().get(childid).getEpisodeString() + "\n" +
					shows.get(groupid).getEpisodes().get(childid).getName());
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.episodemenu, menu);
			if (episodesType != 1) 
			{
				menu.removeItem(R.id.episodeMenuAcquired);
			}
			if (episodesType == 2)
			{
				menu.removeItem(R.id.episodeMenuWatched);
			}
		}
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
			case ABOUT_DIALOG:
				tracker.trackPageView("/aboutDialog");
				dialog = new Dialog(this);
				dialog.setContentView(R.layout.informationsdialog);
				dialog.setTitle(R.string.aboutTitle);
				TextView aboutText = (TextView) dialog.findViewById(R.id.informationtext);
				aboutText.setText(R.string.aboutText);
				Linkify.addLinks(aboutText, Linkify.ALL);
				break;
			case CHANGELOG_DIALOG:
				tracker.trackPageView("/changeLogDialog");
				dialog = new Dialog(this);
				dialog.setContentView(R.layout.informationsdialog);
				dialog.setTitle(R.string.changelogTitle);
				TextView changelogText = (TextView) dialog.findViewById(R.id.informationtext);
				changelogText.setText(R.string.changelogText);
				Linkify.addLinks(changelogText, Linkify.ALL);
				break;
			case LOGOUT_DIALOG:
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
				alertBuilder.setTitle(R.string.logoutDialogTitle)
						   .setMessage(R.string.logoutDialogMessage)
						   .setCancelable(false)
						   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									logout();
								}
							})
						   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
				AlertDialog alertDialog = alertBuilder.create();
				dialog = alertDialog;
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
                
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start("UA-3183255-2", 30, this);
        
		TabMain tabMain = (TabMain) getParent();
        Bundle data = this.getIntent().getExtras();
        episodesType = (Integer) data.getSerializable("Type");
        
        tabMain.clearRefreshTab(episodesType);
        init();
        checkPreferences();
        openLoginActivity();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
            case R.id.refresh:
                reloadEpisodes();
                return true;
            case R.id.preferences:
                openPreferencesActivity();
                return true;
            case R.id.about:
                showDialog(ABOUT_DIALOG);
                return true;
            case R.id.logout:
                showDialog(LOGOUT_DIALOG);
                return true;
            case R.id.changelog:
                showDialog(CHANGELOG_DIALOG);
                return true;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int groupid = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childid = ExpandableListView.getPackedPositionChild(info.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			switch(item.getItemId()) {
			case R.id.episodeMenuWatched:
				tracker.trackEvent("MarkAsWatched", "ContextMenu-EpisodesWatchListActivity", "", 0);
				markEpisodes(0, shows.get(groupid).getEpisodes().get(childid));
				return true;
			case R.id.episodeMenuAcquired:
				tracker.trackEvent("MarkAsAcquire", "ContextMenu-EpisodesWatchListActivity", "", 0);
				markEpisodes(1, shows.get(groupid).getEpisodes().get(childid));
				return true;
			case R.id.episodeMenuDetails:
				tracker.trackPageView("/episodeDetailsSubActivity");
				openEpisodeDetails(shows.get(groupid).getEpisodes().get(childid), episodesType);
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		tracker.trackPageView("/episodeDetailsSubActivity");
		openEpisodeDetails(shows.get(groupPosition).getEpisodes().get(childPosition), episodesType);
		return true;
	}
	
	private void init() {
		setContentView(R.layout.watchlist);
        episodes = new ArrayList<Episode>();
        
        res = getResources();
        conf = res.getConfiguration();
        
        String LanguageCode = Preferences.getPreference(this, PreferencesKeys.LANGUAGE_KEY);
        conf.locale = new Locale(LanguageCode);
        res.updateConfiguration(conf, null);
		
		episodeAdapter = new SimpleExpandableListAdapter(
	                this,
	                createGroups(),
	                R.layout.episoderowgroup,
	                new String[] {"episodeRowTitle"},
	                new int[] { R.id.episodeRowTitle },
	                createChilds(),
	                R.layout.episoderowchild,
	                new String[] {"episodeRowChildTitle", "episodeRowChildDetail"},
	                new int[] { R.id.episodeRowChildTitle, R.id.episodeRowChildDetail }
		);
		setListAdapter(episodeAdapter);
		episodeAdapter.notifyDataSetChanged();
		registerForContextMenu(getExpandableListView());
		
		int countEpisodes = 0;
		for(Show show : shows)
		{
			countEpisodes += show.getNumberEpisodes();
		}
		Title = (TextView) findViewById(R.id.watchListTitle);
		Title.setText(getString(R.string.watchListTitle));
		subTitle = (TextView) findViewById(R.id.watchListSubTitle);
		
		if (countEpisodes == 1)
		{
			switch(episodesType)
			{
			case 0:
		        subTitle.setText(getString(R.string.watchListSubTitleWatch, countEpisodes));
		        break;
			case 1:
			    subTitle.setText(getString(R.string.watchListSubTitleAcquire, countEpisodes));
			    break;
			case 2:
		        subTitle.setText(getString(R.string.watchListSubTitleComing, countEpisodes));
		        break;
			}
		}
		else
		{
			switch(episodesType)
			{
			case 0:
		        subTitle.setText(getString(R.string.watchListSubTitleWatchPlural, countEpisodes));
		        break;
			case 1:
			    subTitle.setText(getString(R.string.watchListSubTitleAcquirePlural, countEpisodes));
			    break;
			case 2:
		        subTitle.setText(getString(R.string.watchListSubTitleComingPlural, countEpisodes));
		        break;
			}
		}
	}
	
	private List<? extends Map<String, ?>> createGroups() {
		List output = new ArrayList();
		
		for(Show show : shows) {
				HashMap map = new HashMap();
				map.put("episodeRowTitle", show.getShowName() + " ( " + show.getNumberEpisodes() + " )");
				output.add(map);
			}

		return output;
	}
	
	private List<? extends List<? extends Map<String, ?>>> createChilds() {
		List subList = new ArrayList();
		for (Show show : shows) {
			List subListSecondLvl = new ArrayList();
			for (Episode episode : show.getEpisodes()) {
					HashMap map = new HashMap();
					map.put("episodeRowChildTitle", episode.getShowName());
					map.put("episodeRowChildDetail", "S" + episode.getSeasonString() + "E" + episode.getEpisodeString() + " - " + episode.getName());
					subListSecondLvl.add(map);
			}
			subList.add(subListSecondLvl);
		}

		return subList;
	}

	private void openLoginActivity() {
		Intent loginSubActivity = new Intent(this.getApplicationContext(), LoginSubActivity.class);
        startActivityForResult(loginSubActivity, LOGIN_REQUEST_CODE);
	}

    private void openPreferencesActivity() {
        tracker.trackPageView("/generalPreferences");
        Intent preferencesActivity = new Intent(this.getApplicationContext(), PreferencesActivity.class);
        startActivity(preferencesActivity);
    }

	private void openEpisodeDetails(Episode episode, int episodesType2) {
		Intent episodeDetailsSubActivity = new Intent(this.getApplicationContext(), EpisodeDetailsSubActivity.class);
		episodeDetailsSubActivity.putExtra("episode", episode);
		episodeDetailsSubActivity.putExtra("episodesType", episodesType2);
        startActivityForResult(episodeDetailsSubActivity, EPISODE_DETAILS_REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            tracker.trackPageView("/episodesWatchListActivity");
	        user = new User(
        		Preferences.getPreference(this, User.USERNAME),
        		Preferences.getPreference(this, User.PASSWORD)
    		);

	        reloadEpisodes();
		} else if (requestCode == EPISODE_DETAILS_REQUEST_CODE) {
			if (resultCode == RESULT_OK)
			{
	            tracker.trackPageView("/episodesWatchListActivity");
	            Bundle intentData = data.getExtras();
	            String markEpisode = intentData.getString("markEpisode");
	            Episode episode = (Episode) intentData.getSerializable("episode");
	
	            if (markEpisode.equals("watch")) {
	                tracker.trackEvent("MarkAsWatched", "MenuButton-DetailsSubActivity", "", 0);
	                markEpisodes(0, episode);
	            }
	            else if (markEpisode.equals("acquire")) {
	                tracker.trackEvent("MarkAsAcquired", "MenuButton-DetailsSubActivity", "", 0);
	            	markEpisodes(1, episode);
	            }
			}
		} else {
			exit();
		}
	}

    private void checkPreferences() {
        //Checks preference for show sorting and sets the default to ascending (A-Z)
        String[] episodeOrderOptions = getResources().getStringArray(R.array.episodeOrderOptionsValues);
        Preferences.checkDefaultPreference(this, PreferencesKeys.EPISODE_SORTING_KEY, episodeOrderOptions[0]);
        //Checks preference for episode sorting and sets default to ascending (oldest episode on top)
        String[] showOrderOptions = getResources().getStringArray(R.array.showOrderOptionsValues);
        Preferences.checkDefaultPreference(this, PreferencesKeys.SHOW_SORTING_KEY, showOrderOptions[0]);
        Preferences.checkDefaultPreference(this, PreferencesKeys.LANGUAGE_KEY, conf.locale.getLanguage());
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
			episodes = myEpisodesService.retrieveEpisodes(episodesType, user);
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
			shows = new ArrayList<Show>();
			if (episodes != null && episodes.size() > 0) {
				for (Episode ep : episodes) {
					AddEpisodeToShow(ep);
				}
			} else {
				Log.d(LOG_TAG, "Episode can't be added to show.");
			}

            sortShows(shows);
            sortEpisodesOfShows(shows);

			dismissDialog(EPISODE_LOADING_DIALOG);
			init();
			

			if (exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
				showDialog(EXCEPTION_DIALOG);
				exceptionMessageResId = null;
			}
		}

		private void AddEpisodeToShow(Episode episode) {
			Show returnShow = CheckShowDublicate(episode.getShowName());
			if (returnShow == null)
			{
				Show tempShow = new Show(episode.getShowName());
				tempShow.addEpisode(episode);
				shows.add(tempShow);
			}
			else
			{
				for(Show show : shows)
				{
					if (show.getShowName().equals(returnShow.getShowName()))
					{
						show.addEpisode(episode);
					}
				}
			}
		}
		
		private Show CheckShowDublicate(String episode)
		{
			for(Show show : shows)
			{
				if (show.getShowName().equals(episode))
				{
					return show;
				}
			}
			return null;
		}
	};

    private void sortShows(List<Show> showList) {
        String sorting = Preferences.getPreference(this, PreferencesKeys.SHOW_SORTING_KEY);
        String[] showOrderOptions = getResources().getStringArray(R.array.showOrderOptionsValues);

        if (sorting.equals(showOrderOptions[1])) {
            Log.d(LOG_TAG, "Sorting episodes ascending");
            Collections.sort(showList, new ShowAscendingComparator());
        } else  if (sorting.equals(showOrderOptions[2])) {
            Log.d(LOG_TAG, "Sorting episodes descending");
            Collections.sort(showList, new ShowDescendingComparator());
        } else if (sorting.equals(showOrderOptions[0])) {
            Log.d(LOG_TAG, "Default my episodes show sorting, nothing to do!");
        }
    }

    private void sortEpisodesOfShows(List<Show> showList) {
        String sorting = Preferences.getPreference(this, PreferencesKeys.EPISODE_SORTING_KEY);
        String[] episodeOrderOptions = getResources().getStringArray(R.array.episodeOrderOptionsValues);

        for (Show show : showList) {
            if (sorting.equals(episodeOrderOptions[0])) {
                Collections.sort(show.getEpisodes(), new EpisodeAscendingComparator());
            } else if(sorting.equals(episodeOrderOptions[1])) {
                Collections.sort(show.getEpisodes(), new EpisodeDescendingComparator());
            }
        }
    }

    private void markEpisodes(final int EpisodeStatus, final Episode episode) {
		showDialog(EPISODE_LOADING_DIALOG);
		markEpisode = new Runnable() {
			@Override
			public void run() {
				markEpisode(EpisodeStatus, episode);
			}
		};
        
		Thread thread =  new Thread(null, markEpisode, "EpisodeMarkWatchedBackground");
		thread.start();
	}
	
	private void markEpisode(int EpisodeStatus, Episode episode) {
		try {
			if (EpisodeStatus == 0)
			{
				myEpisodesService.watchedEpisode(episode, user);
			}
			else if (EpisodeStatus == 1)
			{
				myEpisodesService.acquireEpisode(episode, user);
				TabMain tabMain = (TabMain) getParent();
				tabMain.refreshWatchTab();
			}
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
		tracker.trackEvent("Logout", "MenuButton-EpisodesWatchListActivity", "", 0);
		Preferences.removePreference(this, User.USERNAME);
		Preferences.removePreference(this, User.PASSWORD);
		TabMain tabMain = (TabMain) getParent();
		tabMain.refreshAllTabs(episodesType);
		openLoginActivity();
	}
	
	private void exit() {
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Stop the tracker when it is no longer needed.
		tracker.stop();
	}
}