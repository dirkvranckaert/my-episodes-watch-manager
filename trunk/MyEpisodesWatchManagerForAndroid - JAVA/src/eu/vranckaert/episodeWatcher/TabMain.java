package eu.vranckaert.episodeWatcher;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabMain extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabmain);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 0);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("" + R.string.watch).setIndicator("Watch",
        res.getDrawable(R.drawable.tabwatched)).setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 1);
        spec = tabHost.newTabSpec("" + R.string.acquire).setIndicator("Acquire",
        res.getDrawable(R.drawable.tabacquired)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, EpisodesWatchListActivity.class).putExtra("Type", 2);
        spec = tabHost.newTabSpec("" + R.string.coming).setIndicator("Coming",
        res.getDrawable(R.drawable.tabcoming)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }
}