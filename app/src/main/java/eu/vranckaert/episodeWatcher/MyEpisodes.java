package eu.vranckaert.episodeWatcher;

import org.acra.*;
import org.acra.annotation.*;

import eu.vranckaert.episodeWatcher.guice.Application;

@ReportsCrashes(formKey = "dEc2bkM0dDA3cWYzY1JqQWFvaDNDblE6MQ") 
public class MyEpisodes extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
    }
	
}
