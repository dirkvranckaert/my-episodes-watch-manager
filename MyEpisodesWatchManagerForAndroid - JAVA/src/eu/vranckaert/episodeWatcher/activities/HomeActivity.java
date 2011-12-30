package eu.vranckaert.episodeWatcher.activities;

import java.util.Locale;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.constants.ActivityConstants;
import eu.vranckaert.episodeWatcher.constants.MyEpisodeConstants;
import eu.vranckaert.episodeWatcher.controllers.EpisodesController;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.enums.ListMode;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.pager.HorizontalPager;
import eu.vranckaert.episodeWatcher.pager.PagerControl;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.EpisodesService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class HomeActivity extends Activity {
	private EpisodesService service;
	private User user;
	private Resources res; // Resource object to get Drawables
	private android.content.res.Configuration conf;
	private static final int EPISODE_LOADING_DIALOG = 0;
	private static final int LOGOUT_DIALOG = 1;
	private static final int EXCEPTION_DIALOG = 2;
	private static final int LOGIN_RESULT = 5;
	private static final int SETTINGS_RESULT = 6;
	
	private boolean exception;
	
	private Button btnWatched;
	private Button btnAcquired;
	
	private Intent watchIntent;
	private Intent acquireIntent;
	private Intent comingIntent;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}
	
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        init();
        checkPreferences();
        String LanguageCode = Preferences.getPreference(this, PreferencesKeys.LANGUAGE_KEY);
        
        //fix issue where app run and no days back has been set by the user.
        if(Preferences.getPreference(this, PreferencesKeys.DAYS_BACKWARDCP).equals(null) || Preferences.getPreference(this, PreferencesKeys.DAYS_BACKWARDCP) == ""){        	
        	MyEpisodeConstants.DAYS_BACK_CP = "365";
        	Preferences.setPreference(this, PreferencesKeys.DAYS_BACKWARDCP, MyEpisodeConstants.DAYS_BACK_CP);
        } else {
        	MyEpisodeConstants.DAYS_BACK_CP = Preferences.getPreference(this, PreferencesKeys.DAYS_BACKWARDCP);
        }
        
        MyEpisodeConstants.DAYS_BACK_ENABLED = Preferences.getPreferenceBoolean(this, PreferencesKeys.DAYS_BACKWARD_ENABLED_KEY, false);
        
        conf.locale = new Locale(LanguageCode);
        res.updateConfiguration(conf, null);
        openLoginActivity();
        
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
    	super.onCreate(savedInstanceState);
    	this.service = new EpisodesService();
    	
        setContentView(R.layout.main);
        user = new User(
        		Preferences.getPreference(this, User.USERNAME),
        		Preferences.getPreference(this, User.PASSWORD)
    		);

        final PagerControl control = (PagerControl) findViewById(R.id.control);
        final HorizontalPager pager = (HorizontalPager) findViewById(R.id.pager);
        control.setNumPages(pager.getChildCount());
        
        pager.addOnScrollListener(new HorizontalPager.OnScrollListener() {
            public void onScroll(int scrollX) {
                float scale = (float) (pager.getPageWidth() * pager.getChildCount()) / (float) control.getWidth();
                control.setPosition((int) (scrollX / scale));
            }

            public void onViewScrollFinished(int currentPage) {
                control.setCurrentPage(currentPage);
              
                if (currentPage == 0) 
                	((ImageView) findViewById(R.id.menu_indicator)).setImageResource(R.drawable.home_indicator1);
                else
                	((ImageView) findViewById(R.id.menu_indicator)).setImageResource(R.drawable.home_indicator2);
            }
        });

        String[] showOrderOptions = getResources().getStringArray(R.array.showOrderOptionsValues);
        
    	btnWatched = (Button) findViewById(R.id.btn_watched);
    	watchIntent = new Intent().setClass(this, EpisodeListingActivity.class)
				 .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, EpisodeType.EPISODES_TO_WATCH);
    	String watch_sorting = Preferences.getPreference(this, PreferencesKeys.WATCH_SHOW_SORTING_KEY);
    	if (watch_sorting.equals(showOrderOptions[3])) {
    		watchIntent.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_DATE);
    	} else {
    		watchIntent.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_SHOW);
    	}
    	btnWatched.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(watchIntent);
			}
		});
    	
    	acquireIntent = new Intent().setClass(this, EpisodeListingActivity.class)
    					 .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, EpisodeType.EPISODES_TO_ACQUIRE);
    	String acquire_sorting = Preferences.getPreference(this, PreferencesKeys.ACQUIRE_SHOW_SORTING_KEY);
    	if (acquire_sorting.equals(showOrderOptions[3])) {
    		acquireIntent.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_DATE);
    	} else {
    		acquireIntent.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_SHOW);
    	}
    	btnAcquired = (Button) findViewById(R.id.btn_acquired);
    	btnAcquired.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(acquireIntent);
			}
		});
    	if (Preferences.getPreferenceBoolean(this, PreferencesKeys.DISABLE_ACQUIRE, false)) {
    		btnAcquired.setVisibility(View.GONE);
    	}

        Button btnComing = (Button) findViewById(R.id.btn_coming);
        comingIntent = new Intent().setClass(this, EpisodeListingActivity.class)
				 .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, EpisodeType.EPISODES_COMING);
    	String coming_sorting = Preferences.getPreference(this, PreferencesKeys.COMING_SHOW_SORTING_KEY);
    	if (coming_sorting.equals(showOrderOptions[3])) {
    		comingIntent.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_DATE);
    	} else {
    		comingIntent.putExtra(ActivityConstants.EXTRA_BUILD_VAR_LIST_MODE, ListMode.EPISODES_BY_SHOW);
    	}
        btnComing.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(comingIntent);
			}
		});
    	if (Preferences.getPreferenceBoolean(this, PreferencesKeys.DISABLE_COMING, false)) {
    		btnComing.setVisibility(View.GONE);
    	}
        
        Button btnMore = (Button) findViewById(R.id.btn_more);
        btnMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.scrollRight();
			}
		});
    }
    
    private void getEpisodesInLoadingDialog() {
    	final EpisodesController episodesController = EpisodesController.getInstance();
        user = new User(
        		Preferences.getPreference(this, User.USERNAME),
        		Preferences.getPreference(this, User.PASSWORD)
    		);
    	if (episodesController.areListsEmpty()) {
	    	AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>() {
	
	            @Override
	            protected void onPreExecute() {
	                showDialog(EPISODE_LOADING_DIALOG);
	            }
	
	            @Override
	            protected Object doInBackground(Object... objects) {
	            	try {
	            		episodesController.setEpisodes(EpisodeType.EPISODES_TO_WATCH, service.retrieveEpisodes(EpisodeType.EPISODES_TO_WATCH, user));
	                	String acquire = Preferences.getPreference(HomeActivity.this, PreferencesKeys.ACQUIRE_KEY);
	    	            if (acquire != null && acquire.equals("1")) {
	    	            	EpisodesController.getInstance().setEpisodes(EpisodeType.EPISODES_TO_YESTERDAY1, service.retrieveEpisodes(EpisodeType.EPISODES_TO_YESTERDAY1, user));
	    	            	EpisodesController.getInstance().addEpisodes(EpisodeType.EPISODES_TO_YESTERDAY2, service.retrieveEpisodes(EpisodeType.EPISODES_TO_YESTERDAY2, user));
	    	            } else {
	    	            	EpisodesController.getInstance().setEpisodes(EpisodeType.EPISODES_TO_ACQUIRE, service.retrieveEpisodes(EpisodeType.EPISODES_TO_ACQUIRE, user));
	    	            }
	            		episodesController.setEpisodes(EpisodeType.EPISODES_COMING, service.retrieveEpisodes(EpisodeType.EPISODES_COMING, user));
	        		} catch (InternetConnectivityException e) {
	        			exception = true;
	        		} catch (Exception e) {
	        			e.printStackTrace();
	        		}
	                return 100L;
	            }
	
	            @Override
	            protected void onPostExecute(Object o) {
	                removeDialog(EPISODE_LOADING_DIALOG);
	                
	                if (exception) {
	                	exception = false;
	                	showDialog(EXCEPTION_DIALOG);
	                } else {
	                	btnWatched.setText(getString(R.string.watchhome, EpisodesController.getInstance().getEpisodesCount(EpisodeType.EPISODES_TO_WATCH)));
	            		btnAcquired.setText(getString(R.string.acquirehome, EpisodesController.getInstance().getEpisodesCount(EpisodeType.EPISODES_TO_ACQUIRE)));
	                }
	            }
	        };
	        asyncTask.execute();
    	}
	}

	@Override
    protected void onResume() {
    	super.onResume();
    	btnWatched.setText(getString(R.string.watchhome, EpisodesController.getInstance().getEpisodesCount(EpisodeType.EPISODES_TO_WATCH)));
    	btnAcquired.setText(getString(R.string.acquirehome, EpisodesController.getInstance().getEpisodesCount(EpisodeType.EPISODES_TO_ACQUIRE)));
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_RESULT && resultCode == RESULT_OK)
			getEpisodesInLoadingDialog();
		if (requestCode == SETTINGS_RESULT && resultCode == RESULT_OK) {
			EpisodesController.getInstance().deleteAll();
			finish();
			
	        Intent homeActivity = new Intent(this.getApplicationContext(), HomeActivity.class);
	        startActivity(homeActivity);
		}
		if (requestCode == LOGIN_RESULT && resultCode != RESULT_OK) {
			finish();
		}
	}
    
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
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
			case EXCEPTION_DIALOG:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.exceptionDialogTitle)
					   .setMessage(R.string.internetConnectionFailureTryAgain)
					   .setCancelable(false)
					   .setPositiveButton(R.string.refresh, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                removeDialog(EXCEPTION_DIALOG);
				                getEpisodesInLoadingDialog();
				           }
				       })
				       .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                removeDialog(EXCEPTION_DIALOG);
				                finish();
				           }
				       });
				dialog = builder.create();
				break;
			case EPISODE_LOADING_DIALOG:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.progressLoadingTitle));
                progressDialog.setCancelable(false);
				dialog = progressDialog;
				break;
			default:
				dialog = super.onCreateDialog(id);
				break;
		}
		return dialog;
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
	
    private void init() {
        res = getResources();
        conf = res.getConfiguration();
    }

    /**
     * Check if all preferences exist upon loading the application.
     */
    private void checkPreferences() {
        //Checks preference for show sorting and sets the default to ascending (A-Z)
        String[] episodeOrderOptions = getResources().getStringArray(R.array.episodeOrderOptionsValues);
        Preferences.checkDefaultPreference(this, PreferencesKeys.EPISODE_SORTING_KEY, episodeOrderOptions[0]);
        //Checks preference for episode sorting and sets default to ascending (oldest episode on top)
        String[] showOrderOptions = getResources().getStringArray(R.array.showOrderOptionsValues);
        Preferences.checkDefaultPreference(this, PreferencesKeys.WATCH_SHOW_SORTING_KEY, showOrderOptions[0]);
        Preferences.checkDefaultPreference(this, PreferencesKeys.ACQUIRE_SHOW_SORTING_KEY, showOrderOptions[0]);
        Preferences.checkDefaultPreference(this, PreferencesKeys.COMING_SHOW_SORTING_KEY, showOrderOptions[3]);
        Preferences.checkDefaultPreference(this, PreferencesKeys.LANGUAGE_KEY, conf.locale.getLanguage());
        Preferences.checkDefaultPreference(this, PreferencesKeys.ACQUIRE_KEY, "0");
        Preferences.checkDefaultPreference(this, PreferencesKeys.DAYS_BACKWARDCP, "365");
        Preferences.getPreferenceBoolean(this, PreferencesKeys.DISABLE_COMING, false);
    }
    
    private void openPreferencesActivity() {
        Intent preferencesActivity = new Intent(this.getApplicationContext(), PreferencesActivity.class);
        startActivityForResult(preferencesActivity, SETTINGS_RESULT);
    }
    
    public void onManageClick(View v) {
        Intent manageShowsActivity = new Intent(this.getApplicationContext(), ShowManagementPortalActivity.class);
        startActivity(manageShowsActivity);
    }
    
    public void onLogoutClick(View v) {
    	showDialog(LOGOUT_DIALOG);
    }
    
	private void logout() {
		Preferences.removePreference(this, User.USERNAME);
		Preferences.removePreference(this, User.PASSWORD);
		EpisodesController.getInstance().deleteAll();
		openLoginActivity();
	}
	
	private void openLoginActivity() {
		Intent loginSubActivity = new Intent(this.getApplicationContext(), LoginActivity.class);
        startActivityForResult(loginSubActivity, LOGIN_RESULT);
	}
	
    public void onAboutClick(View v) {
        Intent manageShowsActivity = new Intent(this.getApplicationContext(), AboutActivity.class);
        startActivity(manageShowsActivity);
    }
    
    public void onTodoClick(View v) {
        Intent manageShowsActivity = new Intent(this.getApplicationContext(), ChangelogActivity.class);
        startActivity(manageShowsActivity);
    }
    
    public void onRandomClick(View v) {
        Intent randomActivity = new Intent(this.getApplicationContext(), RandomEpPickerActivity.class);
        startActivity(randomActivity);
    }
}
