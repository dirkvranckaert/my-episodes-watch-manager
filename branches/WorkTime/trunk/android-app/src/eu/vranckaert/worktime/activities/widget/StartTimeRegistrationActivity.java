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
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.WidgetService;
import roboguice.activity.GuiceActivity;

import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 23:25
 */
public class StartTimeRegistrationActivity extends GuiceActivity {
    private static final String LOG_TAG = StartTimeRegistrationActivity.class.getSimpleName();

    @Inject
    private WidgetService widgetService;

    @Inject
    private TimeRegistrationService timeRegistrationService;

    @Inject
    private ProjectService projectService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Started the START TimeRegistration acitivity");

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

                Date startTime = new Date();
                Project selectedProject = projectService.getSelectedProject();

                TimeRegistration newTr = new TimeRegistration();
                newTr.setProject(selectedProject);
                newTr.setStartTime(startTime);
                timeRegistrationService.create(newTr);

                widgetService.updateWidget(StartTimeRegistrationActivity.this);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                removeDialog(Constants.Dialog.LOADING_TIMEREGISTRATION_CHANGE);
                Toast.makeText(StartTimeRegistrationActivity.this, R.string.msg_widget_time_reg_created, Toast.LENGTH_LONG).show();
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
                Log.d(LOG_TAG, "Creating loading dialog for starting a new time registration");
                dialog = ProgressDialog.show(
                        StartTimeRegistrationActivity.this,
                        "",
                        getString(R.string.lbl_widget_starting_new_timeregistration),
                        true,
                        false
                );
                break;
            }
        };
        return dialog;
    }
}
