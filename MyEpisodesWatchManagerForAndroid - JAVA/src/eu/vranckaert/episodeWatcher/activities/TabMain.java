package eu.vranckaert.episodeWatcher.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import eu.vranckaert.episodeWatcher.Constants.ActivityConstants;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.enums.EpisodeListingType;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;

import java.util.Locale;

public class TabMain extends TabActivity {
	private TabHost tabHost;
	private Intent intentWatch;  // Reusable Intent for each tab
	private Intent intentAcquire;  // Reusable Intent for each tab
	private Intent intentComing;  // Reusable Intent for each tab
	private Resources res; // Resource object to get Drawables
	private android.content.res.Configuration conf;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.tabmain);
        
        tabHost = getTabHost();  // The activity TabHost        
        res = getResources();
        conf = res.getConfiguration();
        
    	Preferences.checkDefaultPreference(this, PreferencesKeys.LANGUAGE_KEY, conf.locale.getLanguage());
        
        String languageCode = Preferences.getPreference(this, PreferencesKeys.LANGUAGE_KEY);
        conf.locale = new Locale(languageCode);
        res.updateConfiguration(conf, null);
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intentWatch = new Intent().setClass(this, EpisodesWatchListActivity.class)
        					 .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, EpisodeListingType.EPISODES_TO_WATCH)
        					 .setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        // Initialize a TabSpec for each tab and add it to the TabHost
        TabHost.TabSpec watchSpec = tabHost.newTabSpec("" + R.string.watch)
        			  .setIndicator(getString(R.string.watch), res.getDrawable(R.drawable.tabwatched))
        			  .setContent(intentWatch);

        intentAcquire = new Intent().setClass(this, EpisodesWatchListActivity.class)
        					 .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, EpisodeListingType.EPISODES_TO_ACQUIRE)
        					 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        TabHost.TabSpec acquireSpec = tabHost.newTabSpec("" + R.string.acquire)
        			  .setIndicator(getString(R.string.acquire), res.getDrawable(R.drawable.tabacquired))
        			  .setContent(intentAcquire);

        intentComing = new Intent().setClass(this, EpisodesWatchListActivity.class)
        					 .putExtra(ActivityConstants.EXTRA_BUNLDE_VAR_EPISODE_TYPE, EpisodeListingType.EPISODES_COMING)
        					 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        TabHost.TabSpec comingSpec = tabHost.newTabSpec("" + R.string.coming)
        			  .setIndicator(getString(R.string.coming), res.getDrawable(R.drawable.tabcoming))
        			  .setContent(intentComing);

        int preferedTab = Preferences.getPreferenceInt(this, PreferencesKeys.OPEN_DEFAULT_TAB_KEY);

        if(preferedTab == EpisodeListingType.EPISODES_TO_ACQUIRE.getEpisodeListingType()) {
            tabHost.addTab(acquireSpec);
            tabHost.addTab(watchSpec);
            tabHost.addTab(comingSpec);
        } else if(preferedTab == EpisodeListingType.EPISODES_COMING.getEpisodeListingType()) {
            tabHost.addTab(comingSpec);
            tabHost.addTab(watchSpec);
            tabHost.addTab(acquireSpec);
        } else {
            tabHost.addTab(watchSpec);
            tabHost.addTab(acquireSpec);
            tabHost.addTab(comingSpec);
        }

        tabHost.setCurrentTab(EpisodeListingType.EPISODES_TO_WATCH.getEpisodeListingType());
    }
    
    public void clearRefreshTab(EpisodeListingType episodesType)
    {
    	if (episodesType.equals(EpisodeListingType.EPISODES_TO_WATCH))
    	{
    		intentWatch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    	}
    	else if (episodesType.equals(EpisodeListingType.EPISODES_TO_ACQUIRE))
    	{
    		intentAcquire.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    	}
    	else if (episodesType.equals(EpisodeListingType.EPISODES_COMING))
    	{
    		intentComing.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    	}
    }
    
    public void refreshAllTabs(EpisodeListingType episodesType)
    {
    	if (!episodesType.equals(EpisodeListingType.EPISODES_TO_WATCH))
    	{
    		intentWatch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	}
    	if (!episodesType.equals(EpisodeListingType.EPISODES_TO_ACQUIRE))
    	{
    		intentAcquire.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	}
    	if (!episodesType.equals(EpisodeListingType.EPISODES_COMING))
    	{
    		intentComing.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	}
    }
    
    public void refreshAllTabs()
    {
		intentWatch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intentAcquire.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intentComing.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    
    public void refreshWatchTab()
    {
    	intentWatch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}