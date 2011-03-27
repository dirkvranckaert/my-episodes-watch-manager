package eu.vranckaert.worktime.guice;

import android.util.Log;
import eu.vranckaert.worktime.dao.ProjectDao;
import eu.vranckaert.worktime.dao.TimeRegistrationDao;
import eu.vranckaert.worktime.dao.impl.ProjectDaoImpl;
import eu.vranckaert.worktime.dao.impl.TimeRegistrationDaoImpl;
import eu.vranckaert.worktime.service.DevelopmentService;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.WidgetService;
import eu.vranckaert.worktime.service.impl.DevelopmentServiceImpl;
import eu.vranckaert.worktime.service.impl.ProjectServiceImpl;
import eu.vranckaert.worktime.service.impl.TimeRegistrationServiceImpl;
import eu.vranckaert.worktime.service.impl.WidgetServiceImpl;
import roboguice.config.AbstractAndroidModule;

public class Module extends AbstractAndroidModule {
    private static final String LOG_TAG = Module.class.getSimpleName();

    @Override
    protected void configure() {
        Log.i(LOG_TAG, "Configuring module " + getClass().getSimpleName());

        bindDaos();
        bindServices();

        Log.i(LOG_TAG, "DAO's and services are now bound!");
    }

    private void bindDaos() {
        bind(TimeRegistrationDao.class).to(TimeRegistrationDaoImpl.class);
        bind(ProjectDao.class).to(ProjectDaoImpl.class);
    }

    private void bindServices() {
        bind(DevelopmentService.class).to(DevelopmentServiceImpl.class);
        bind(ProjectService.class).to(ProjectServiceImpl.class);
        bind(TimeRegistrationService.class).to(TimeRegistrationServiceImpl.class);
        //Widget service
        bind(WidgetService.class).to(WidgetServiceImpl.class);
    }
}
