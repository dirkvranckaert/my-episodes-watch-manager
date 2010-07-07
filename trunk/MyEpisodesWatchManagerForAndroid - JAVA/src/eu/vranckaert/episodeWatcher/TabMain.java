package eu.vranckaert.episodeWatcher;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabMain extends TabActivity {
	private TabHost tabHost;
	private TabHost.TabSpec spec;
	private Intent intent;  // Reusable Intent for each tab
	private Resources res; // Resource object to get Drawables
	//private TabController tabController;
	//private EpisodesWatchListActivity episodesWatchListActivity;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//episodesWatchListActivity = new EpisodesWatchListActivity(tabController);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabmain);
        
        tabHost = getTabHost();  // The activity TabHost
        res = getResources();
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        //intent = new Intent().setClass(this, episodesWatchListActivity.getClass()).putExtra("Type", 0);
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 0);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("" + R.string.watch).setIndicator("Watch",
        res.getDrawable(R.drawable.tabwatched)).setContent(intent);
        tabHost.addTab(spec);
        
        // Do the same for the other tabs
        //intent = new Intent().setClass(this, episodesWatchListActivity.getClass()).putExtra("Type", 1);
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 1);
        spec = tabHost.newTabSpec("" + R.string.acquire).setIndicator("Acquire",
        res.getDrawable(R.drawable.tabacquired)).setContent(intent);
        tabHost.addTab(spec);
		
        //intent = new Intent().setClass(this, episodesWatchListActivity.getClass()).putExtra("Type", 2);
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 2);
        spec = tabHost.newTabSpec("" + R.string.coming).setIndicator("Coming",
        res.getDrawable(R.drawable.tabcoming)).setContent(intent);
        tabHost.addTab(spec);
    }
    
    public void refreshTabs(int currentTab)
    {
    	tabHost.clearAllTabs();
    	
        // Create an Intent to launch an Activity for the tab (to be reused)
        //intent = new Intent().setClass(this, episodesWatchListActivity.getClass()).putExtra("Type", 0);
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 0);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("" + R.string.watch).setIndicator("Watch",
        res.getDrawable(R.drawable.tabwatched)).setContent(intent);
        tabHost.addTab(spec);
        
        // Do the same for the other tabs
        //intent = new Intent().setClass(this, episodesWatchListActivity.getClass()).putExtra("Type", 1);
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 1);
        spec = tabHost.newTabSpec("" + R.string.acquire).setIndicator("Acquire",
        res.getDrawable(R.drawable.tabacquired)).setContent(intent);
        tabHost.addTab(spec);
        
        //intent = new Intent().setClass(this, episodesWatchListActivity.getClass()).putExtra("Type", 2);
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 2);
        spec = tabHost.newTabSpec("" + R.string.coming).setIndicator("Coming",
        res.getDrawable(R.drawable.tabcoming)).setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(currentTab);
    }
}