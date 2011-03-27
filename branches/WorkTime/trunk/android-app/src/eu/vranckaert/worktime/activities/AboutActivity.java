package eu.vranckaert.worktime.activities;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.dao.utils.DaoConstants;
import eu.vranckaert.worktime.utils.IntentUtil;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.string.StringUtils;
import roboguice.activity.GuiceActivity;
import roboguice.inject.InjectView;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 19:06
 */
public class AboutActivity extends GuiceActivity {

    @InjectView(R.id.about_project_text) TextView projectText;
    @InjectView(R.id.about_version_name_text) TextView versionNameText;
    @InjectView(R.id.about_version_code_text) TextView versionCodeText;
    @InjectView(R.id.about_database_name_text) TextView databaseNameText;
    @InjectView(R.id.about_database_version_text) TextView databaseVersionText;
    @InjectView(R.id.about_website_text) TextView websiteText;
    @InjectView(R.id.about_bug_tracking_website_text) TextView bugTrackingWebsiteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String project = getString(R.string.app_name);
        String versionName = ContextUtils.getCurrentApplicationVersionName(AboutActivity.this);
        int versionCode = ContextUtils.getCurrentApplicationVersionCode(AboutActivity.this);

        int databaseVersionCode = DaoConstants.VERSION;
        String databaseName  = DaoConstants.DATABASE;

        String website = "http://code.google.com/p/worktime/";
        String bugTrackingWebsite = "http://code.google.com/p/worktime/issues/entry";

        projectText.setText(StringUtils.SPACE + project);
        versionNameText.setText(StringUtils.SPACE + versionName);
        versionCodeText.setText(StringUtils.SPACE + String.valueOf(versionCode));
        databaseNameText.setText(StringUtils.SPACE + databaseName);
        databaseVersionText.setText(StringUtils.SPACE + String.valueOf(databaseVersionCode));
        websiteText.setText(StringUtils.SPACE + website);
        bugTrackingWebsiteText.setText(StringUtils.SPACE + bugTrackingWebsite);

        Linkify.addLinks(websiteText, Linkify.WEB_URLS);
        Linkify.addLinks(bugTrackingWebsiteText, Linkify.WEB_URLS);
    }

    public void onHomeClick(View view) {
        IntentUtil.goHome(this);
    }
}
