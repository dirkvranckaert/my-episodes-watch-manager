package eu.vranckaert.worktime.service.impl;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.HomeActivity;
import eu.vranckaert.worktime.activities.widget.SelectProjectActivity;
import eu.vranckaert.worktime.activities.widget.StartTimeRegistrationActivity;
import eu.vranckaert.worktime.activities.widget.StopTimeRegistrationActivity;
import eu.vranckaert.worktime.dao.TimeRegistrationDao;
import eu.vranckaert.worktime.dao.impl.TimeRegistrationDaoImpl;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.providers.WorkTimeWidgetProvider;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.WidgetService;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 19:13
 */
public class WidgetServiceImpl implements WidgetService {
    private static final String LOG_TAG = WidgetServiceImpl.class.getName();

    TimeRegistrationDao timeRegistrationDao;
    ProjectService projectService;
    RemoteViews views;

    /**
     * {@inheritDoc}
     */
    public void updateWidget(Context ctx) {
        getServices(ctx);
        getDaos(ctx);
        getViews(ctx);

        boolean timeRegistrationStarted = false;

        int numberOfTimeRegs = timeRegistrationDao.countTotalNumberOfTimeRegistrations();

        TimeRegistration lastTimeRegistration = null;
        if(numberOfTimeRegs > 0) {
            lastTimeRegistration = timeRegistrationDao.getLatestTimeRegistration();
            Log.d(LOG_TAG, "The last time registration has ID " + lastTimeRegistration.getId());
        } else {
            Log.d(LOG_TAG, "No timeregstrations found yet!");
        }

        /* TODO found a bug, when clicking a lot after each-other on the start button, things get messy.
         * Reason: upon receiving the onclick (the 2nd, 3rd, 4th,... time) the handler is not yet changed/or the widget
         * is not yet updated which for example makes that you press 2 times start and the 3rd you press stop.
         * Result:
         *  starttime ok
         *  endtime ok
         * --------------
         *  starttime ok
         *  endtime NULL
         * If, after the 3rd click, you are quick enough press a 4th time the stop button is still there and shows a
         * message about corrupt data!
         *
         * A way to fix this is to use one activity for all start/stop onclicks and everytime in the activity check
         * the status and determine what to do next.
         *
         * Another way to fix it, hmmm, still to be found...
         */
        if(numberOfTimeRegs == 0 || (lastTimeRegistration != null && lastTimeRegistration.getEndTime() != null)) {
            Log.d(LOG_TAG, "No timeregistrations found yet or it's an ended timeregistration");
            views.setCharSequence(R.id.widget_actionbtn, "setText", ctx.getString(R.string.btn_widget_start));
            //Enable on click for the start button
            Log.d(LOG_TAG, "Couple the start button to an on click action");
            startBackgroundWorkActivity(ctx, R.id.widget_actionbtn, StartTimeRegistrationActivity.class, null);
        } else if(lastTimeRegistration != null && lastTimeRegistration.getEndTime() == null) {
            Log.d(LOG_TAG, "This is an ongoing timeregistration");
            views.setCharSequence(R.id.widget_actionbtn, "setText", ctx.getString(R.string.btnw_widget_stop));
            //Enable on click for the stop button
            Log.d(LOG_TAG, "Couple the stop button to an on click action.");
            startBackgroundWorkActivity(ctx, R.id.widget_actionbtn, StopTimeRegistrationActivity.class, null);
            timeRegistrationStarted = true;
        }

        //Update the selected project
        Project project = projectService.getSelectedProject();
        views.setCharSequence(R.id.widget_projectname, "setText", project.getName());

        //Enable on click for the entire widget to open the app
        Log.d(LOG_TAG, "Couple the widget background to an on click action. On click opens the home activity");
        Intent homeAppIntent = new Intent(ctx, HomeActivity.class);
        PendingIntent homeAppPendingIntent = PendingIntent.getActivity(ctx, 0, homeAppIntent, 0);
        views.setOnClickPendingIntent(R.id.widget, homeAppPendingIntent);

        //Enable on click for the widget title to open the app if a registration is just started, or to open the
        //"select project" popup to change the selected project.
        Log.d(LOG_TAG, "Couple the widget title background to an on click action.");
        if (timeRegistrationStarted) {
            Log.d(LOG_TAG, "On click opens the home activity");
            views.setOnClickPendingIntent(R.id.widget_bgtop, homeAppPendingIntent);
        } else {
            Log.d(LOG_TAG, "On click opens a chooser-dialog for selecting the a project");
            startBackgroundWorkActivity(ctx, R.id.widget_bgtop, SelectProjectActivity.class, null);
        }

        commitView(ctx, views);
    }

    /**
     * Starts an activity that should do something in the background after clicking a button on the widget. That doesn't
     * mean that the activity cannot ask the user for any input/choice/... It only means that the launched
     * {@link Intent} by default enables on flag: {@link Intent#FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS} which forces the
     * activity to not be shown in the recent launched apps/activities. Other flags can be defined in the method call.
     * @param ctx The widget's context.
     * @param resId The resource id of the view on the widget on which to bind the on click action.
     * @param activity The activity that will do some background processing.
     * @param extraFlags Extra flags for the activities.
     */
    private void startBackgroundWorkActivity(Context ctx, int resId, Class<? extends Activity> activity, int... extraFlags) {
        int defaultFlag = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;

        Intent intent = new Intent(ctx, activity);
        intent.setFlags(defaultFlag);

        if(extraFlags != null) {
            for (int flag : extraFlags) {
                if (flag != defaultFlag) {
                    intent.setFlags(flag);
                }
            }
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        views.setOnClickPendingIntent(resId, pendingIntent);
    }

    /**
     * Every RemoteViews that is updated should be comitted!
     * @param ctx The context.
     * @param updatedView The update view.
     */
    private void commitView(Context ctx, RemoteViews updatedView) {
        Log.d(LOG_TAG, "Comitting update view...");
        ComponentName cn = new ComponentName(ctx, WorkTimeWidgetProvider.class);
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
		mgr.updateAppWidget(cn, updatedView);
        Log.d(LOG_TAG, "Updated view comitted!");
    }

    /**
     * Find the views to be updated.
     * @param ctx The widget's context.
     */
    private void getViews(Context ctx) {
        this.views = new RemoteViews(ctx.getPackageName(), R.layout.worktime_appwidget);
        Log.d(LOG_TAG, "I just got the view which we'll start updating!");
    }

    /**
     * Create all the required DAO instances.
     * @param ctx The widget's context.
     */
    private void getDaos(Context ctx) {
        this.timeRegistrationDao = new TimeRegistrationDaoImpl(ctx);
        Log.d(LOG_TAG, "DAOS ok!");
    }

    /**
     * Create all the required service instances.
     * @param ctx The widget's context.
     */
    private void getServices(Context ctx) {
        this.projectService = new ProjectServiceImpl(ctx);
        Log.d(LOG_TAG, "Services ok!");
    }
}
