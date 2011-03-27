package eu.vranckaert.worktime.activities.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.WidgetService;
import roboguice.activity.GuiceActivity;

import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 23:25
 */
public class StopTimeRegistrationActivity extends GuiceActivity {
    private static final String LOG_TAG = StopTimeRegistrationActivity.class.getSimpleName();

    @Inject
    WidgetService widgetService;

    @Inject
    TimeRegistrationService timeRegistrationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Started the STOP TimeRegistration acitivity");

        AsyncTask threading = new AsyncTask() {

            @Override
            protected void onPreExecute() {
                showDialog(Constants.Dialog.LOADING_TIMEREGISTRATION_CHANGE);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(LOG_TAG, "Is there already a looper? " + (Looper.myLooper() != null));
                if(Looper.myLooper() == null) {
                    Looper.prepare();
                }

                Date endTime = new Date();

                TimeRegistration latestRegistration = timeRegistrationService.getLatestTimeRegistration();

                if (latestRegistration == null || latestRegistration.getEndTime() != null) {
                    Log.w(LOG_TAG, "Data must be incorrupt! Please clear all the data through the system settings of the application!");
                    return new Object();
                } else {
                    latestRegistration.setEndTime(endTime);
                    timeRegistrationService.update(latestRegistration);

                    widgetService.updateWidget(StopTimeRegistrationActivity.this);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.LOADING_TIMEREGISTRATION_CHANGE);
                Log.d(LOG_TAG, "Loading dialog removed from UI");
                if (o != null) {
                    Log.d(LOG_TAG, "Something went wrong...");
                    Toast.makeText(StopTimeRegistrationActivity.this, R.string.err_widget_corrupt_data, Toast.LENGTH_LONG).show();
                } else if (o == null) {
                    Log.d(LOG_TAG, "Successfully ended time registration");
                    Toast.makeText(StopTimeRegistrationActivity.this, R.string.msg_widget_time_reg_ended, Toast.LENGTH_LONG).show();
                }
                Log.d(LOG_TAG, "Finishing activity...");
                finish();
            }
        };
        threading.execute();
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch(dialogId) {
            case Constants.Dialog.LOADING_TIMEREGISTRATION_CHANGE: {
                Log.d(LOG_TAG, "Creating loading dialog for ending the active time registration");
                dialog = ProgressDialog.show(
                        StopTimeRegistrationActivity.this,
                        "",
                        getString(R.string.lbl_widget_ending_timeregistration),
                        true,
                        false
                );
                break;
            }
        };
        return dialog;
    }
}
