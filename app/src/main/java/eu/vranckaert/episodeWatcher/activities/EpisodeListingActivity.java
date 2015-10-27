package eu.vranckaert.episodeWatcher.activities;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.constants.ActivityConstants;
import eu.vranckaert.episodeWatcher.controllers.EpisodesController;
import eu.vranckaert.episodeWatcher.controllers.RowController;
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
import roboguice.activity.GuiceExpandableListActivity;

import java.util.*;

/**
 * @author Ivo Janssen
 */

public class EpisodeListingActivity extends GuiceExpandableListActivity {
	private static final int EPISODE_LOADING_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 2;
	private static final int SETTINGS_RESULT = 6;
	private static final String LOG_TAG = EpisodeListingActivity.class.getSimpleName();

	private User user;
    private EpisodesService service;
    private List<Episode> episodes = new ArrayList<Episode>();
    private List<Show> shows = new ArrayList<Show>();
    private TextView Title;
    private TextView subTitle;
    private SimpleExpandableListAdapter episodeAdapter;
    private Integer exceptionMessageResId = null;
    private static EpisodeType episodesType;
    private ListMode listMode;
	private Resources res; // Resource object to get Drawables
	private android.content.res.Configuration conf;
    private Map<Date, List<Episode>> listedAirDates = null;
    private boolean collapsed = true;

    private CustomAnalyticsTracker tracker;

	public EpisodeListingActivity() {
		super();
		this.service = new EpisodesService();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	        case R.id.preferences:
	            openPreferencesActivity();
	            return true;
		}
		return false;
	}

	@Override
    protected void onResume() {
    	super.onResume();    		
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SETTINGS_RESULT && resultCode == RESULT_OK) {
			EpisodesController.getInstance().deleteAll();
			finish();
			
			startActivity(new Intent(getApplicationContext(), HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)); 
		}
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
			inflater.inflate(R.menu.episode_listing_tab_child_list_menu, menu);
			
			if (episodesType.equals(EpisodeType.EPISODES_COMING)) {
				menu.removeItem(R.id.episodeTweet);
				menu.removeItem(R.id.episodeMenuAcquired);
			} else if  (episodesType.equals(EpisodeType.EPISODES_TO_WATCH)) {
				menu.removeItem(R.id.episodeMenuAcquired);
			}
		} else {
			int groupid = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	        switch(listMode) {
	            case EPISODES_BY_SHOW:
	            	menu.setHeaderTitle(shows.get(groupid).getShowName());
	                break;
	            case EPISODES_BY_DATE:
	            	menu.setHeaderTitle(DateUtil.formatDateLong(determineDate(groupid), this));
	                break;
	        }
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.episode_listing_tab_group_list_menu, menu);
			
			if (episodesType.equals(EpisodeType.EPISODES_COMING)) {
				menu.removeItem(R.id.showMenuAcquired);
				menu.removeItem(R.id.showMenuWatched);
			} else if  (episodesType.equals(EpisodeType.EPISODES_TO_WATCH)) {
				menu.removeItem(R.id.showMenuAcquired);
			}
		}
	}

    @Override
    public void onPause() {
    	super.onPause();
    	saveListRows();
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
    
    private List<Episode> determineGroup(int listGroupId) {
    	List<Episode> episodes = null;

        if(listGroupId < 0) {
            return null;
        }
        
        switch(listMode) {
            case EPISODES_BY_SHOW:
                episodes = shows.get(listGroupId).getEpisodes();
                break;
            case EPISODES_BY_DATE:
            	Iterator iter = listedAirDates.entrySet().iterator();
                int i = 0;
                while(iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    if(i == listGroupId) {
                        episodes = listedAirDates.get(entry.getKey());
                        break;
                    } else {
                        i++;
                    }
                }
                break;
        }
        return episodes;
    }
    
    private Date determineDate(int listGroupId) {
    	List<Episode> episodes = null;

        if(listGroupId < 0) {
            return null;
        }
        
        switch(listMode) {
            case EPISODES_BY_SHOW:
                return shows.get(listGroupId).getEpisodes().get(0).getAirDate();
		case EPISODES_BY_DATE:
            	Iterator iter = listedAirDates.entrySet().iterator();
                int i = 0;
                while(iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    if(i == listGroupId) {
                        return (Date) entry.getKey();
                    } else {
                        i++;
                    }
                }
                break;
        }
        return null;
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case EPISODE_LOADING_DIALOG:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.progressLoadingTitle));
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
			default:
				dialog = super.onCreateDialog(id);
				break;
		}
		return dialog;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
        super.onCreate(savedInstanceState);
        
        tracker = CustomAnalyticsTracker.getInstance(this);
        Bundle data = this.getIntent().getExtras();
        episodesType = (EpisodeType) data.getSerializable(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE);
        listMode = (ListMode) data.getSerializable(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE);
        
        init();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int groupid = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childid = ExpandableListView.getPackedPositionChild(info.packedPosition);
        Episode selectedEpisode = determineEpisode(groupid, childid);
        List<Episode> selectedGroup = null;
    	selectedGroup = determineGroup(groupid);
			switch(item.getItemId()) {
			case R.id.episodeMenuWatched:
                tracker.trackEvent(CustomTracker.Event.MARK_WATCHED);
                markEpisodes(0, selectedEpisode);
				return true;
			case R.id.episodeMenuAcquired:
				tracker.trackEvent(CustomTracker.Event.MARK_ACQUIRED);
				markEpisodes(1, selectedEpisode);
				return true;
			case R.id.episodeTweet:
		    	String tweet = selectedEpisode.getShowName() + " S" + selectedEpisode.getSeasonString() + "E" + selectedEpisode.getEpisodeString() + " - " + selectedEpisode.getName();
		    	Intent i = new Intent(android.content.Intent.ACTION_SEND);
		    	i.setType("text/plain");
		    	i.putExtra(Intent.EXTRA_TEXT, getString(R.string.Tweet, tweet));
		    	startActivity(Intent.createChooser(i, getString(R.string.TweetTitle)));
				return true;
			case R.id.episodeMenuDetails:
				tracker.trackPageView(CustomTracker.PageView.EPISODE_DETAILS);
				openEpisodeDetails(selectedEpisode, episodesType);
				return true;
			case R.id.showMenuWatched:
				markEpisodes(0, selectedGroup);
				return true;
			case R.id.showMenuAcquired:
				markEpisodes(1, selectedGroup);
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
		setContentView(R.layout.episode_listing_tab);
        episodes = new ArrayList<Episode>();
        
        user = new User(
        		Preferences.getPreference(this, User.USERNAME),
        		Preferences.getPreference(this, User.PASSWORD)
    		);
        res = getResources();
        conf = res.getConfiguration();

        String LanguageCode = Preferences.getPreference(this, PreferencesKeys.LANGUAGE_KEY);
        conf.locale = new Locale(LanguageCode);
        res.updateConfiguration(conf, null);
        
    	Title = (TextView) findViewById(R.id.watchListTitle);
    	Title.setText(getString(R.string.watchListTitle));
    	subTitle = (TextView) findViewById(R.id.watchListSubTitle);
    	subTitle.setText("");
    	
    	Bundle data = this.getIntent().getExtras();
    	String markEpisode = data.getString(ActivityConstants.EXTRA_BUNDLE_VAR_MARK_EPISODE);
    	
    	if (markEpisode != null && markEpisode != "") {
	        Episode episode = (Episode) data.getSerializable(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE);
	        
	        if (markEpisode.equals(ActivityConstants.EXTRA_BUNDLE_VALUE_WATCH)) {
	            tracker.trackEvent(CustomTracker.Event.MARK_WATCHED);
	            markEpisodes(0, episode);
	        }
	        else if (markEpisode.equals(ActivityConstants.EXTRA_BUNDLE_VALUE_ACQUIRE)) {
	            tracker.trackEvent(CustomTracker.Event.MARK_ACQUIRED);
	            markEpisodes(1, episode);
	        }
    	}
		getEpisodes();
		returnEpisodes();
	}
	
	private void initExendableList()
	{
		episodeAdapter = new SimpleExpandableListAdapter(
	                this,
	                createGroups(),
	                R.layout.episode_listing_tab_row_group,
	                new String[] {"episodeRowTitle"},
	                new int[] { R.id.episodeRowTitle },
	                createChilds(),
	                R.layout.episode_listing_tab_row_child,
	                new String[] {"episodeRowChildTitle", "episodeRowChildDetail"},
	                new int[] { R.id.episodeRowChildTitle, R.id.episodeRowChildDetail }
		);
		setListAdapter(episodeAdapter);
		episodeAdapter.notifyDataSetChanged();
		registerForContextMenu(getExpandableListView());
        
    	((ImageView) findViewById(R.id.separator_collapse)).setVisibility(View.VISIBLE);
    	((ImageButton) findViewById(R.id.btn_title_collapse)).setVisibility(View.VISIBLE);
		
		int countEpisodes = EpisodesController.getInstance().getEpisodesCount(episodesType);
		
        if (countEpisodes == 200)
        	Toast.makeText(EpisodeListingActivity.this, R.string.watchListFull, Toast.LENGTH_LONG).show();

        if (countEpisodes == 1) {
            switch(episodesType) {
            case EPISODES_TO_WATCH:
                subTitle.setText(getString(R.string.watchListSubTitleWatch, countEpisodes));
                ((TextView) findViewById(R.id.title_text)).setText(R.string.watch);
                openListRows(RowController.getInstance().getOpenWatchRows());
                break;
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
            case EPISODES_TO_ACQUIRE:
                subTitle.setText(getString(R.string.watchListSubTitleAcquire, countEpisodes));
                ((TextView) findViewById(R.id.title_text)).setText(R.string.acquire);
                openListRows(RowController.getInstance().getOpenAcquireRows());
                break;
            case EPISODES_COMING:
                subTitle.setText(getString(R.string.watchListSubTitleComing, countEpisodes));
                ((TextView) findViewById(R.id.title_text)).setText(R.string.coming);
                openListRows(RowController.getInstance().getOpenComingRows());
                break;
            }
        }
        else {
            switch(episodesType) {
            case EPISODES_TO_WATCH:
                subTitle.setText(getString(R.string.watchListSubTitleWatchPlural, countEpisodes));
                ((TextView) findViewById(R.id.title_text)).setText(R.string.watch);
                openListRows(RowController.getInstance().getOpenWatchRows());
                break;
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
            case EPISODES_TO_ACQUIRE:
                subTitle.setText(getString(R.string.watchListSubTitleAcquirePlural, countEpisodes));
                ((TextView) findViewById(R.id.title_text)).setText(R.string.acquire);
                openListRows(RowController.getInstance().getOpenAcquireRows());
                break;
            case EPISODES_COMING:
                subTitle.setText(getString(R.string.watchListSubTitleComingPlural, countEpisodes));
                ((TextView) findViewById(R.id.title_text)).setText(R.string.coming);
                openListRows(RowController.getInstance().getOpenComingRows());
                break;
            }
        }
	}

	private void openListRows(List<String> openRows) {
		for (String rowName : openRows) {
			for (int i = 0; i<episodeAdapter.getGroupCount(); i++) {
				if (rowName.equals(((String[]) episodeAdapter.getGroup(i).toString().split("\\["))[0]))
					this.getExpandableListView().expandGroup(i);
			}
		}
	}
	
	private void saveListRows() {
		List<String> rows = new ArrayList<String>();
		
		for (int i = 0; i<episodeAdapter.getGroupCount(); i++) {
			if (this.getExpandableListView().collapseGroup(i)) {
				rows.add(((String[]) episodeAdapter.getGroup(i).toString().split("\\["))[0]);
			}
				
		}
		
	    switch(episodesType) {
		    case EPISODES_TO_WATCH:
		        RowController.getInstance().setOpenWatchRows(rows);
		        break;
			case EPISODES_TO_YESTERDAY1:
			case EPISODES_TO_YESTERDAY2:
		    case EPISODES_TO_ACQUIRE:
		    	RowController.getInstance().setOpenAcquireRows(rows);
		        break;
		    case EPISODES_COMING:
		    	RowController.getInstance().setOpenComingRows(rows);
		        break;
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
            
            for(Date key : workingMap.keySet()) {
                Map<String, String> map = new HashMap<String, String>();
                int countEp = workingMap.get(key);
                map.put("episodeRowTitle", DateUtil.formatDateFull(key, getApplicationContext()) + " [ " + countEp + " ]");
                headerList.add(map);
                listedAirDates.put(key, null);
            }
            break;
            }
            case EPISODES_BY_SHOW: {
                for(Show show : shows) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("episodeRowTitle", show.getShowName() + " [ " + show.getNumberEpisodes() + " ]");
                    headerList.add(map);
                }
            }
        }

		return headerList;
	}

	private List<? extends List<? extends Map<String, ?>>> createChilds() {
		List<List<Map<String,String>>> childList = new ArrayList<List<Map<String,String>>>();

        switch(listMode) {
        case EPISODES_BY_DATE: {
            for(Iterator iter = listedAirDates.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                Date listedAirDate = (Date) entry.getKey();
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
            break;
            }
        	case EPISODES_BY_SHOW: {
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
            break;
        	}
        }
		return childList;
	}

	private void openEpisodeDetails(Episode episode, EpisodeType episodeType) {
		finish();
		
		Intent episodeDetailsSubActivity = new Intent(this.getApplicationContext(), EpisodeDetailsActivity.class);
		episodeDetailsSubActivity.putExtra(ActivityConstants.EXTRA_BUNDLE_VAR_EPISODE, episode)
								 .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, episodeType);
        startActivity(episodeDetailsSubActivity);
	}
	
    private void openPreferencesActivity() {
        Intent preferencesActivity = new Intent(this.getApplicationContext(), PreferencesActivity.class);
        startActivityForResult(preferencesActivity, SETTINGS_RESULT);
    }

    private void reloadEpisodes() {
    	saveListRows();    	
    	
    	AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>()  {
            @Override
            protected void onPreExecute() {
                showDialog(EPISODE_LOADING_DIALOG);
            }

            @Override
            protected Object doInBackground(Object... objects) {
            	getEpisodesMyEpisodes();
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
		episodes = EpisodesController.getInstance().getEpisodes(episodesType);
	}
    
	private void getEpisodesMyEpisodes() {
		try {
        	if (episodesType == EpisodeType.EPISODES_TO_ACQUIRE) {
        		String acquire = Preferences.getPreference(EpisodeListingActivity.this, PreferencesKeys.ACQUIRE_KEY);
	            if (acquire != null && acquire.equals("1")) {
	            	EpisodesController.getInstance().setEpisodes(EpisodeType.EPISODES_TO_YESTERDAY1, service.retrieveEpisodes(EpisodeType.EPISODES_TO_YESTERDAY1, user));
	            	EpisodesController.getInstance().addEpisodes(EpisodeType.EPISODES_TO_YESTERDAY2, service.retrieveEpisodes(EpisodeType.EPISODES_TO_YESTERDAY2, user));
	            } else {
	            	EpisodesController.getInstance().setEpisodes(EpisodeType.EPISODES_TO_ACQUIRE, service.retrieveEpisodes(EpisodeType.EPISODES_TO_ACQUIRE, user));
	            }
        	} else {
        		EpisodesController.getInstance().setEpisodes(episodesType, service.retrieveEpisodes(episodesType, user));
        	}
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
		
		getEpisodes();
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
    	String sorting = "";
    	switch(episodesType) {
        case EPISODES_TO_WATCH:
        	sorting = Preferences.getPreference(this, PreferencesKeys.WATCH_SHOW_SORTING_KEY);
            break;
		case EPISODES_TO_YESTERDAY1:
		case EPISODES_TO_YESTERDAY2:
        case EPISODES_TO_ACQUIRE:
        	sorting = Preferences.getPreference(this, PreferencesKeys.ACQUIRE_SHOW_SORTING_KEY);
            break;
        case EPISODES_COMING:
        	sorting = Preferences.getPreference(this, PreferencesKeys.COMING_SHOW_SORTING_KEY);
            break;
        }
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
    	AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>() {

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
        		removeDialog(EPISODE_LOADING_DIALOG);
                if (exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
                    showDialog(EXCEPTION_DIALOG);
                    exceptionMessageResId = null;
                } else {
                	EpisodesController.getInstance().deleteEpisode(episode.getType(), episode);
                    returnEpisodes();
                }
            }
        };
        asyncTask.execute();
	}
    
    private void markEpisodes(final int episodeStatus, final List<Episode> episodes) {
    	AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>() {

            @Override
            protected void onPreExecute() {
                showDialog(EPISODE_LOADING_DIALOG);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                markAllEpisodes(episodeStatus, episodes);
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
                    removeDialog(EPISODE_LOADING_DIALOG);
                    returnEpisodes();
                }
            }
        };
        asyncTask.execute();
	}
    
    private void markShowEpisodes(final int episodeStatus, final Show show) {
    	AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>() {

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
                    removeDialog(EPISODE_LOADING_DIALOG);
                    returnEpisodes();
                }
            }
        };
        asyncTask.execute();
	}

	private void markEpisode(int EpisodeStatus, Episode episode) {
		try {
			switch(EpisodeStatus) {
				case 0: service.watchedEpisode(episode, user); break;
				case 1: service.acquireEpisode(episode, user); break;
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
			switch(EpisodeStatus) {
				case 0: service.watchedEpisodes(episodes, user); break;
				case 1: service.acquireEpisodes(episodes, user); break;
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
	
	public void onHomeClick(View v) {
		exit();
	}
	
	public void onRefreshClick(View v) {
		reloadEpisodes();
	}
	
	public void onCollapseClick(View v) {
		for (int i=0; i<episodeAdapter.getGroupCount(); i++) {
			if (collapsed) {
				this.getExpandableListView().expandGroup(i);
				((ImageButton) findViewById(R.id.btn_title_collapse)).setImageResource(R.drawable.ic_title_collapse2);
			} else {
				this.getExpandableListView().collapseGroup(i);
				((ImageButton) findViewById(R.id.btn_title_collapse)).setImageResource(R.drawable.ic_title_collapse);
			}
		}
		
		collapsed = !collapsed;
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