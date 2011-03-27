package eu.vranckaert.worktime.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.service.DevelopmentService;
import roboguice.activity.GuiceActivity;

public class HomeActivity extends GuiceActivity {
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    @Inject
    private DevelopmentService developmentService;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        developmentService.bootCheck();
    }

    private void launchActivity(Class activity) {
        Intent intent = new Intent(getApplicationContext(), activity);
        startActivity(intent);
    }

    public void onTimeRegistrationsClick(View view) {
        launchActivity(TimeRegistrationsActivity.class);
    }

    public void onProjectsClick(View view) {
        launchActivity(ManageProjectsActivity.class);
    }

    public void onPreferencesClick(View view) {
        launchActivity(PreferencesActivity.class);
    }

    public void onAboutClick(View view) {
        launchActivity(AboutActivity.class);
    }
}
