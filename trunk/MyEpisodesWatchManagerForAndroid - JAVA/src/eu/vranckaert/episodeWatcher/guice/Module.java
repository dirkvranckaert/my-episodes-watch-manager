package eu.vranckaert.episodeWatcher.guice;

import android.util.Log;
import roboguice.config.AbstractAndroidModule;

public class Module extends AbstractAndroidModule {
    private static final String LOG_TAG = Module.class.getSimpleName();

    @Override
    protected void configure() {
        Log.i(LOG_TAG, "Configuring module " + getClass().getSimpleName());

        bindDaos();
        Log.d(LOG_TAG, "DAO's are bound!");
        bindServices();
        Log.d(LOG_TAG, "Services are bound!");

        Log.i(LOG_TAG, "DAO's and services are now bound!");
    }

    private void bindDaos() {
        //bind(PersonDao.class).to(PersonDaoImpl.class);
    }

    private void bindServices() {
        //bind(PersonService.class).to(PersonServiceImpl.class);
    }
}
