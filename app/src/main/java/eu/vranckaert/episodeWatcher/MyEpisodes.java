package eu.vranckaert.episodeWatcher;

import android.content.Context;
import org.acra.*;
import org.acra.annotation.*;

import eu.vranckaert.episodeWatcher.guice.Application;

@ReportsCrashes(formKey = "dEc2bkM0dDA3cWYzY1JqQWFvaDNDblE6MQ") 
public class MyEpisodes extends Application {

    private static MyEpisodes INSTANCE;

    public MyEpisodes() {
        INSTANCE = this;
    }

    public static MyEpisodes getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MyEpisodes();
        }

        return INSTANCE;
    }

    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
        super.onCreate();
    }

    public static final Context getContext() {
        return getInstance().getApplicationContext();
    }
	
}
