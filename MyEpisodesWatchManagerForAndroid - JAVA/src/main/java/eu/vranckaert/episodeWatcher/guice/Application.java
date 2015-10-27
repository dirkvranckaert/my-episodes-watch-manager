package eu.vranckaert.episodeWatcher.guice;

import android.util.Log;
import roboguice.application.GuiceApplication;

import java.util.List;

public class Application extends GuiceApplication {
    public static final String LOG_TAG = Application.class.getSimpleName();

    @Override
    protected void addApplicationModules(List<com.google.inject.Module> modules) {
        Log.i(LOG_TAG, "Starting RoboGuice...");
        modules.add(new Module());
        Log.i(LOG_TAG, "Initializing RoboGuice finished!");
    }
}
