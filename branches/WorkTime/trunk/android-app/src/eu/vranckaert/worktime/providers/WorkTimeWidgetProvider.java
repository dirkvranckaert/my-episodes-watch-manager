package eu.vranckaert.worktime.providers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import eu.vranckaert.worktime.service.WidgetService;
import eu.vranckaert.worktime.service.impl.WidgetServiceImpl;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/02/11
 * Time: 20:58
 */
public class WorkTimeWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = WorkTimeWidgetProvider.class.getName();

    private WidgetService service;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "RECEIVE");
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "UPDATE");
        Log.d(LOG_TAG, "Number of widgets found: " + appWidgetIds.length);

        service = new WidgetServiceImpl();

        for(int appWidgetId : appWidgetIds) {
            AppWidgetProviderInfo widgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
            Log.d(LOG_TAG, "STARTING FOR WIDGET ID: " + appWidgetId);
            Log.d(LOG_TAG, "PROVIDER: " + widgetProviderInfo.provider.toString());

            service.updateWidget(context);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(LOG_TAG, "DELETED");
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(LOG_TAG, "ENABLED");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(LOG_TAG, "DISABLED");
        super.onDisabled(context);
    }
}
