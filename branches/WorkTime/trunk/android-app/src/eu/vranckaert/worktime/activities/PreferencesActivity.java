package eu.vranckaert.worktime.activities;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.View;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.utils.IntentUtil;
import roboguice.activity.GuicePreferenceActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 19:09
 */
public class PreferencesActivity extends GuicePreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        configurePreferences(PreferencesActivity.this);
        createPreferences(PreferencesActivity.this);
    }

    private void configurePreferences(GuicePreferenceActivity ctx) {
        ctx.getPreferenceManager().setSharedPreferencesName(Constants.Preferences.PREFERENCES_NAME);
    }

    private void createPreferences(GuicePreferenceActivity ctx) {
        PreferenceScreen preferences = ctx.getPreferenceManager().createPreferenceScreen(ctx);
        setPreferenceScreen(preferences);

        PreferenceCategory exportCategory = new PreferenceCategory(ctx);
        exportCategory.setTitle(R.string.pref_export_category_title);
        preferences.addPreference(exportCategory);

        createExportCategoryPreferences(ctx, exportCategory);
    }

    private void createExportCategoryPreferences(GuicePreferenceActivity ctx, PreferenceCategory exportCategory) {
        EditTextPreference exportFileName = new EditTextPreference(ctx);
        exportFileName.setDefaultValue(Constants.Preferences.EXPORT_TIME_REG_FILE_NAME_DEFAULT_VALUE);
        exportFileName.setKey(Constants.Preferences.Keys.EXPORT_TIME_REG_FILE_NAME);
        exportFileName.setTitle(R.string.pref_export_time_reg_file_name_title);
        exportFileName.setSummary(R.string.pref_export_time_reg_file_name_summary);
        exportCategory.addPreference(exportFileName);
    }

    public void onHomeClick(View view) {
        IntentUtil.goHome(this);
    }
}
