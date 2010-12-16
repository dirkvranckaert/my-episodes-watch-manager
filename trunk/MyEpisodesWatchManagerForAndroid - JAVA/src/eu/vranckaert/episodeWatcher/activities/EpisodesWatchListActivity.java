package eu.vranckaert.episodeWatcher.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.Constants.ActivityConstants;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.*;
import eu.vranckaert.episodeWatcher.enums.CustomTracker;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.enums.ListMode;
import eu.vranckaert.episodeWatcher.exception.*;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.EpisodesService;
import eu.vranckaert.episodeWatcher.utils.CustomAnalyticsTracker;
import eu.vranckaert.episodeWatcher.utils.DateUtil;
import java.util.*;

public class EpisodesWatchListActivity extends ExpandableListActivity {
	private static final int LOGIN_REQUEST_CODE = 0;
	private static final int EPISODE_DETAILS_REQUEST_CODE = 1;

	private static final int EPISODE_LOADING_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 1;
	private static final int LOGOUT_DIALOG = 4;
	private static final String LOG_TAG = "EpisodeWatchListActivity";

	private User user;
    private EpisodesService service;
    private List<Episode> episodes = new ArrayList<Episode>();
    private List<Show> shows = new ArrayList<Show>();
    private TextView Title;
    private TextView subTitle;
    private SimpleExpandableListAdapter episodeAdapter;
    private Integer exceptionMessageResId = null;
    private EpisodeType episodesType;
    private ListMode listMode;
	private Resources res; // Resource object to get Drawables
	private android.content.res.Configuration conf;
    private Map<Date, List<Episode>> listedAirDates = null;

    private CustomAnalyticsTracker tracker;

	public EpisodesWatchListActivity() {
		super();
		this.user = new User("myUsername", "myPassword");
		this.service = new EpisodesService();
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

            Episode selectedEpisode = determineEpisode(groupid, childid);

			menu.setHeaderTitle(selectedEpisode.getShowName() +
					" S" + selectedEpisode.getSeasonString() +
					"E" + selectedEpisode.getEpisodeString() + "\n" +
					selectedEpisode.getName());
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.episodemenu, menu);
			if (!episodesType.equals(EpisodeType.EPISODES_TO_ACQUIRE)) {
				menu.removeItem(R.id.episodeMenuAcquired);
			}
			if (episodesType.equals(EpisodeType.EPISODES_COMING)) {
				menu.removeItem(R.id.episodeMenuWatched);
			}
		} else if (listMode.equals(ListMode.EPISODES_BY_SHOW)) {
			int groupid = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			menu.setHeaderTitle(shows.get(groupid).getShowName());
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.groupmenu, menu);
			if (!episodesType.equals(EpisodeType.EPISODES_TO_ACQUIRE)) {
				menu.removeItem(R.id.showMenuAcquired);
			}
			if (episodesType.equals(EpisodeType.EPISODES_COMING)) {
				menu.removeItem(R.id.showMenuWatched);
			}
		}
	}

    private Episode determineEpisode(int listGroupId, int listChildId) {
        Episode episode = null;

        if(listGroupId < 0 || listChildId < 0) {
            return null;
        }
        
        switch(listMode) {
            case EPISODES_BY_SHOW:
                episode = shows.get(listGroupId).getEpisodes().get(listChildId);
                break;
            case EPISODES_BY_DATE:
                Iterator iter = listedAirDates.entrySet().iterator();
                int i = 0;
                while(iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    if(i == listGroupId) {
                        episode = listedAirDates.get(entry.getKey()).get(listChildId);
                        break;
                    } else {
                        i++;
                    }
                }
                break;
        }
        return episode;
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case EPISODE_LOADING_DIALOG:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.progressLoadingTitle));
				//progressDialog.setTitle(R.string.progressLoadingTitle);
                progressDialog.setCancelable(false);
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
				                removeDialog(EXCEPTION_DIALOG);
				           }
				       });
				dialog = builder.create();
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

        tracker = CustomAnalyticsTracker.getInstance(this);

		TabMain tabMain = (TabMain) getParent();
        Bundle data = this.getIntent().getExtras();
        episodesType = (EpisodeType) data.getSerializable(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE);
        listMode = (ListMode) data.getSerializable(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE);

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
            case R.id.manageShows:
                openManageShowsActivity();
                return true;
            case R.id.preferences:
                openPreferencesActivity();
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(this.getApplicationContext(), AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.logout:
                showDialog(LOGOUT_DIALOG);
                return true;
            case R.id.whatsnew:
                Intent whatsNewIntent = new Intent(this.getApplicationContext(), WhatsNewActivity.class);
                startActivity(whatsNewIntent);
                return true;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int groupid = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childid = ExpandableListView.getPackedPositionChild(info.packedPosition);
        Episode selectedEpisode = determineEpisode(groupid, childid);
        Show selectedShow = null;
        if(!listMode.equals(ListMode.EPISODES_BY_DATE)) {
            selectedShow = shows.get(groupid);
        }
			switch(item.getItemId()) {
			case R.id.episodeMenuWatched:
                tracker.trackEvent(CustomTracker.Event.MARK_WATCHED);
				markEpisodes(0, selectedEpisode);
				return true;
			case R.id.episodeMenuAcquired:
				tracker.trackEvent(CustomTracker.Event.MARK_ACQUIRED);
				markEpisodes(1, selectedEpisode);
				return true;
			case R.id.episodeMenuDetails:
				tracker.trackPageView(CustomTracker.PageView.EPISODE_DETAILS);
				openEpisodeDetails(selectedEpisode, episodesType);
				return true;
			case R.id.showMenuWatched:
				markShowEpisodes(0, selectedShow);
				return true;
			case R.id.showMenuAcquired:
				markShowEpisodes(1, selectedShow);
				return true;
			default:
				return false;
			}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		tracker.trackPageView(CustomTracker.PageView.EPISODE_DETAILS);
		openEpisodeDetails(determineEpisode(groupPosition, childPosition), episodesType);
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
        
    	Title = (TextView) findViewById(R.id.watchListTitle);
    	Title.setText(getString(R.string.watchListTitle));
    	subTitle = (TextView) findViewById(R.id.watchListSubTitle);
    	subTitle.setText("");
	}
	
	private void initExendableList()
	{
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
	for(Show show : shows) {
		countEpisodes += show.getNumberEpisodes();
	}

	if (countEpisodes == 1) {
		switch(episodesType) {
		case EPISODES_TO_WATCH:
	        subTitle.setText(getString(R.string.watchListSubTitleWatch, countEpisodes));
	        break;
		case EPISODES_TO_ACQUIRE:
		    subTitle.setText(getString(R.string.watchListSubTitleAcquire, countEpisodes));
		    break;
		case EPISODES_COMING:
	        subTitle.setText(getString(R.string.watchListSubTitleComing, countEpisodes));
	        break;
		}
	}
	else {
		switch(episodesType) {
		case EPISODES_TO_WATCH:
	        subTitle.setText(getString(R.string.watchListSubTitleWatchPlural, countEpisodes));
	        break;
		case EPISODES_TO_ACQUIRE:
		    subTitle.setText(getString(R.string.watchListSubTitleAcquirePlural, countEpisodes));
		    break;
		case EPISODES_COMING:
	        subTitle.setText(getString(R.string.watchListSubTitleComingPlural, countEpisodes));
	        break;
		}
	}
	}

	private List<? extends Map<String, ?>> createGroups() {
		List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();

        switch(listMode) {
            case EPISODES_BY_DATE: {
                listedAirDates = new LinkedHashMap<Date, List<Episode>>();
                Map<Date, Integer> workingMap = new TreeMap<Date, Integer>();
                for (Show show : shows) {
                	for(Episode episode : show.getEpisodes()) {
                		Date airDate = episode.getAirDate();
                		if(!workingMap.containsKey(airDate)) {
                			workingMap.put(airDate, 1);
                		} else {
                			int count = workingMap.get(airDate);
                			workingMap.put(airDate, ++count);
                		}
                	}
                }
                
                for(Iterator iter = workingMap.entrySet().iterator(); iter.hasNext();) {
                	Map<String, String> map = new HashMap<String, String>();
                	Map.Entry entry = (Map.Entry) iter.next();
                	Date date = (Date) entry.getKey();
                	int countEp = (Integer) entry.getValue();
                    Calendar rightNow = Calendar.getInstance();
                    Date now = rightNow.getTime();
                    if (date.after(now)){
	                    map.put("episodeRowTitle", DateUtil.formatDateFull(date, getApplicationContext()) + " ( " + countEp + " )");
	                    headerList.add(map);
	                    listedAirDates.put(date, null);
                    }
                }
                break;
            }
            case EPISODES_BY_SHOW: {
                for(Show show : shows) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("episodeRowTitle", show.getShowName() + " ( " + show.getNumberEpisodes() + " )");
                    headerList.add(map);
                }
            }
        }

		return headerList;
	}

	private List<? extends List<? extends Map<String, ?>>> createChilds() {
		List<List<Map<String,String>>> childList = new ArrayList<List<Map<String,String>>>();

        switch(episodesType) {
            case EPISODES_COMING: {
                for(Iterator iter = listedAirDates.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Date listedAirDate = (Date) entry.getKey();
                    Calendar rightNow = Calendar.getInstance();
                    Date now = rightNow.getTime();
                    if (listedAirDate.after(now)){
                    List<Episode> episodeList = new ArrayList<Episode>();

                    List<Map<String, String>> subListSecondLvl = new ArrayList<Map<String, String>>();
                    for(Show show : shows) {
                        for(Episode episode : show.getEpisodes()) {
                            if(listedAirDate.equals(episode.getAirDate())) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("episodeRowChildTitle", episode.getShowName());
                                map.put("episodeRowChildDetail", "S" + episode.getSeasonString() + "E" + episode.getEpisodeString() + " - " + episode.getName());
                                subListSecondLvl.add(map);
                                episodeList.add(episode);
                            }
                        }
                    }

                    entry.setValue(episodeList);
                    childList.add(subListSecondLvl);
                    }
                }
                break;
            }
            default:
                for (Show show : shows) {
                    List<Map<String, String>> subListSecondLvl = new ArrayList<Map<String, String>>();
                    for (Episode episode : show.getEpisodes()) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("episodeRowChildTitle", episode.getShowName());
                            map.put("episodeRowChildDetail", "S" + episode.getSeasonString() + "E" + episode.getEpisodeString() + " - " + episode.getName());
                            subListSecondLvl.add(map);
                    }
                    childList.add(subListSecondLvl);
                }
        }

		return childList;
	}

	private void openLoginActivity() {
		Intent loginSubActivity = new Intent(this.getApplicationContext(), LoginSubActivity.class);
        startActivityForResult(loginSubActivity, LOGIN_REQUEST_CODE);
	}

    private void openPreferencesActivity() {
        tracker.trackPageView(CustomTracker.PageView.PREFERENCES_GENERAL);
        Intent preferencesActivity = new Intent(this.getApplicationContext(), PreferencesActivity.class);
        startActivity(preferencesActivity);
    }

    private void openManageShowsActivity() {
        tracker.trackPageView(CustomTracker.PageView.SHOW_MANAGEMENT);
        Intent manageShowsActivity = new Intent(this.getApplicationContext(), ShowManagementActivity.class);
        startActivity(manageShowsActivity);
    }

	private void openEpisodeDetails(Episode episode, EpisodeType episodeType) {
		Intent episodeDetailsSubActivity = new Intent(this.getApplicationContext(), EpisodeDetailsSubActivity.class);
		episodeDetailsSubActivity.putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE, episode);
		episodeDetailsSubActivity.putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, episodeType);
        startActivityForResult(episodeDetailsSubActivity, EPISODE_DETAILS_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            tracker.trackPageView(CustomTracker.PageView.EPISODE_LIST);
	        user = new User(
        		Preferences.getPreference(this, User.USERNAME),
        		Preferences.getPreference(this, User.PASSWORD)
    		);

	        reloadEpisodes();
		} else if (requestCode == EPISODE_DETAILS_REQUEST_CODE) {
			if (resultCode == RESULT_OK)
			{
	            tracker.trackPageView(CustomTracker.PageView.EPISODE_LIST);
	            Bundle intentData = data.getExtras();
	            String markEpisode = intentData.getString(ActivityConstants.EXTRA_BUNDLE_VAR_MARK_EPISODE);
	            Episode episode = (Episode) intentData.getSerializable(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE);

	            if (markEpisode.equals(ActivityConstants.EXTRA_BUNDLE_VALUE_WATCH)) {
	                tracker.trackEvent(CustomTracker.Event.MARK_WATCHED);
	                markEpisodes(0, episode);
	            }
	            else if (markEpisode.equals(ActivityConstants.EXTRA_BUNDLE_VALUE_AQUIRE)) {
	                tracker.trackEvent(CustomTracker.Event.MARK_ACQUIRED);
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
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                showDialog(EPISODE_LOADING_DIALOG);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                getEpisodes();
                return 100L;
            }

            @Override
            protected void onPostExecute(Object o) {
                returnEpisodes();
                removeDialog(EPISODE_LOADING_DIALOG);
            }
        };
        asyncTask.execute();
	}

	private void getEpisodes() {
		try {
			episodes = service.retrieveEpisodes(episodesType, user);
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
	}

	private void returnEpisodes() {
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

        initExendableList();

        if (exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
            showDialog(EXCEPTION_DIALOG);
            exceptionMessageResId = null;
        }
    }

    private void AddEpisodeToShow(Episode episode) {
        Show currentShow = CheckShowDublicate(episode.getShowName());
        if (currentShow == null) {
            Show tempShow = new Show(episode.getShowName());
            tempShow.addEpisode(episode);
            shows.add(tempShow);
        }
        else {
        	currentShow.addEpisode(episode);
        }
    }

    private Show CheckShowDublicate(String episodename)
    {
        for(Show show : shows)
        {
            if (show.getShowName().equals(episodename)) {
                return show;
            }
        }
        return null;
    }

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

    private void markEpisodes(final int EpisodeStatus, final Episode episode) {   //TODO use enum
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(EPISODE_LOADING_DIALOG);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                markEpisode(EpisodeStatus, episode);
                if (exceptionMessageResId == null || exceptionMessageResId.equals("")) {
                    getEpisodes();
                }
                return 100L;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
                    removeDialog(EPISODE_LOADING_DIALOG);
                    showDialog(EXCEPTION_DIALOG);
                    exceptionMessageResId = null;
                } else {
                    returnEpisodes();
                    removeDialog(EPISODE_LOADING_DIALOG);
                }
            }
        };
        asyncTask.execute();
	}
    
    private void markShowEpisodes(final int episodeStatus, final Show show) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(EPISODE_LOADING_DIALOG);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                markAllEpisodes(episodeStatus, show.getEpisodes());
                if (exceptionMessageResId == null || exceptionMessageResId.equals("")) {
                    getEpisodes();
                }
                return 100L;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
                    removeDialog(EPISODE_LOADING_DIALOG);
                    showDialog(EXCEPTION_DIALOG);
                    exceptionMessageResId = null;
                } else {
                    returnEpisodes();
                    removeDialog(EPISODE_LOADING_DIALOG);
                }
            }
        };
        asyncTask.execute();
	}

	private void markEpisode(int EpisodeStatus, Episode episode) {
		try {
			if (EpisodeStatus == 0)
			{
				service.watchedEpisode(episode, user);
			}
			else if (EpisodeStatus == 1)
			{
				service.acquireEpisode(episode, user);
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
	}

    private void markAllEpisodes(int EpisodeStatus, List<Episode> episodes) {
		try {
			if (EpisodeStatus == 0)
			{
				service.watchedEpisodes(episodes, user);
			}
			else if (EpisodeStatus == 1)
			{
				service.acquireEpisodes(episodes, user);
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
			String message = "Marking shows watched failed";
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
	}

	private void logout() {
		tracker.trackEvent(CustomTracker.Event.LOGOUT);
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
		tracker.stop();
	}
}