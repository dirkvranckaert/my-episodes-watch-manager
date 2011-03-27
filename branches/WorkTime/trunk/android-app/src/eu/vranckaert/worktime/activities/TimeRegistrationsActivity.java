package eu.vranckaert.worktime.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.timeregistrations.ExportTimeRegistrationsActivity;
import eu.vranckaert.worktime.comparators.TimeRegistrationDescendingByStartdate;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.service.WidgetService;
import eu.vranckaert.worktime.utils.IntentUtil;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.date.DateTimeFormats;
import eu.vranckaert.worktime.utils.date.DateUtils;
import org.joda.time.Period;
import roboguice.activity.GuiceListActivity;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 18:58
 */
public class TimeRegistrationsActivity extends GuiceListActivity {
    private static final String LOG_TAG = TimeRegistrationsActivity.class.getSimpleName();

    @Inject
    TimeRegistrationService timeRegistrationService;
    @Inject
    WidgetService widgetService;

    List<TimeRegistration> timeRegistrations;
    //Vars for deleting time registrations
    TimeRegistration timeRegistrationToDelete = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrations);

        loadTimeRegistrations(true);
    }

    private void loadTimeRegistrations(boolean reloadTimeRegistrations) {
        if (reloadTimeRegistrations) {
            this.timeRegistrations = timeRegistrationService.findAll();
            Collections.sort(timeRegistrations, new TimeRegistrationDescendingByStartdate());
            Log.d(LOG_TAG, this.timeRegistrations.size() + " timeregistrations loaded!");
        }
        TimRegistrationsListAdapter adapter = new TimRegistrationsListAdapter(timeRegistrations);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }

    /**
     * Go Home.
     * @param view The view.
     */
    public void onHomeClick(View view) {
        IntentUtil.goHome(this);
    }

    /**
     * Add a time registration.
     * @param view The view.
     */
    public void onAddClick(View view) {
        //Not yet implemented
    }

    /**
     * Export the time registrations.
     * @param view The view.
     */
    public void onExportClick(View view) {
        if(ContextUtils.isSdCardAvailable() && ContextUtils.isSdCardWritable()) {
            Intent intent = new Intent(TimeRegistrationsActivity.this, ExportTimeRegistrationsActivity.class);
            startActivity(intent);
        } else {
            showDialog(Constants.Dialog.EXPORT_UNAVAILABLE);
        }
    }

    //TimRegistrationsListAdapter
    /**
     * The list adapater private inner-class used to display the manage projects list.
     */
    private class TimRegistrationsListAdapter extends ArrayAdapter<TimeRegistration> {
        private final String LOG_TAG = TimRegistrationsListAdapter.class.getSimpleName();
        /**
         * {@inheritDoc}
         */
        public TimRegistrationsListAdapter(List<TimeRegistration> timeRegistrations) {
            super(TimeRegistrationsActivity.this, R.layout.list_item_time_registrations, timeRegistrations);
            Log.d(LOG_TAG, "Creating the time registrations list adapater");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(LOG_TAG, "Start rendering/recycling row " + position);
            View row;
            final TimeRegistration tr = timeRegistrations.get(position);
            Log.d(LOG_TAG, "Got time registration with startDate " +
                    DateUtils.convertDateTimeToString(tr.getStartTime(),
                    DateTimeFormats.FULL,
                    TimeRegistrationsActivity.this));

            if (convertView == null) {
                Log.d(LOG_TAG, "Render a new line in the list");
                row = getLayoutInflater().inflate(R.layout.list_item_time_registrations, parent, false);
            } else {
                Log.d(LOG_TAG, "Recycling an existing line in the list");
                row = convertView;
            }

            Log.d(LOG_TAG, "Ready to update the startdate, enddate and projectname of the timeregistration...");
            TextView startDate = (TextView) row.findViewById(R.id.lbl_timereg_startdate);
            startDate.setText(DateUtils.convertDateTimeToString(tr.getStartTime(), DateTimeFormats.MEDIUM, TimeRegistrationsActivity.this));
            TextView endDate = (TextView) row.findViewById(R.id.lbl_timereg_enddate);
            String endDateStr = "";
            if(tr.getEndTime() == null) {
                endDateStr = getString(R.string.now);
            } else {
                endDateStr = DateUtils.convertDateTimeToString(tr.getEndTime(), DateTimeFormats.MEDIUM, TimeRegistrationsActivity.this);
            }
            endDate.setText(endDateStr);
            TextView projectName = (TextView) row.findViewById(R.id.lbl_timereg_projectname);
            projectName.setText(tr.getProject().getName());

            Log.d(LOG_TAG, "Ready to update the duration of the timeregistration...");
            TextView durationView = (TextView) row.findViewById(R.id.lbl_timereg_duration);
            String durationText = calculatePeriod(tr);
            durationView.setText(durationText);

            Log.d(LOG_TAG, "Bind an on click event on the delete button");
            View deleteButton =  row.findViewById(R.id.btn_delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    deleteTimeRegistration(tr, true);
                }
            });

            Log.d(LOG_TAG, "Done rendering row " + position);
            return row;
        }
    }

    private String calculatePeriod(TimeRegistration registration) {
        Period period = null;
        if (registration.isOngoingTimeRegistration()) {
            period = DateUtils.calculatePeriod(registration.getStartTime(), new Date());
        } else {
            period = DateUtils.calculatePeriod(registration.getStartTime(), registration.getEndTime());
        }

        int hours = period.getHours();
        int minutes = period.getMinutes();
        int seconds = period.getSeconds();

        String hoursString = hours + " " + getString(R.string.hours) + ", ";
        String minutesString = minutes + " " + getString(R.string.minutes) + ", ";
        String secondsString = seconds + " " + getString(R.string.seconds);
        String periodString;
        if (hours > 0) {
            periodString = hoursString + minutesString + secondsString;
        } else if (minutes > 0) {
            periodString = minutesString + secondsString;
        } else {
            periodString = secondsString;
        }
        return periodString;
    }

    private void deleteTimeRegistration(final TimeRegistration timeRegistration, boolean askPermission) {
        if(askPermission) {
            timeRegistrationToDelete = timeRegistration;
            showDialog(Constants.Dialog.DELETE_TIME_REGISTRATION_YES_NO);
            return;
        }

        timeRegistrationService.remove(timeRegistration);
        timeRegistrations.remove(timeRegistration);
        timeRegistrationToDelete = null;
        widgetService.updateWidget(TimeRegistrationsActivity.this);
        loadTimeRegistrations(false);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case Constants.Dialog.DELETE_TIME_REGISTRATION_YES_NO: {
                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
				alertRemoveAllRegs
						   .setMessage(R.string.msg_delete_registration_confirmation)
						   .setCancelable(false)
						   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    deleteTimeRegistration(timeRegistrationToDelete, false);
                                    dialog.cancel();
								}
							})
						   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    timeRegistrationToDelete = null;
									dialog.cancel();
								}
							});
				dialog = alertRemoveAllRegs.create();
                break;
            }
            case Constants.Dialog.EXPORT_UNAVAILABLE: {
                AlertDialog.Builder alertExportUnavailable = new AlertDialog.Builder(this);
				alertExportUnavailable.setTitle(R.string.msg_export_not_available)
                           .setMessage(R.string.msg_export_not_available_detail)
						   .setCancelable(false)
						   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   dialog.cancel();
                               }
                           });
				dialog = alertExportUnavailable.create();
                break;
            }
        }
        return dialog;
    }
}
