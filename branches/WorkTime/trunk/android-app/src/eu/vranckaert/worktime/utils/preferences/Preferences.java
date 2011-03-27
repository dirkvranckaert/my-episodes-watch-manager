package eu.vranckaert.worktime.utils.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.enums.export.CsvSeparator;
import eu.vranckaert.worktime.enums.export.FileType;

/**
 * Access all the preferences. This class is mainly used to read the preferences but can be used in some rare cases
 * to also update or insert preferences.
 *
 * User: DIRK VRANCKAERT
 * Date: 19/02/11
 * Time: 14:22
 */
public class Preferences {
    /**
     * Get an instance of {@link SharedPreferences} to access the preferences.
     * @param ctx The context when accessing the preferences.
     * @return The instance based on the context.
     */
    private static final SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(Constants.Preferences.PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * Get the preference for key {@link Constants.Preferences.Keys#EXPORT_TIME_REG_FILE_NAME}.
     * @param ctx The context when accessing the preference.
     * @return The {@link String} value for the key.
     */
    public static String getTimeRegistrationExportFileName(Context ctx) {
        SharedPreferences preferences = getSharedPreferences(ctx);
        return preferences.getString(
                Constants.Preferences.Keys.EXPORT_TIME_REG_FILE_NAME,
                Constants.Preferences.EXPORT_TIME_REG_FILE_NAME_DEFAULT_VALUE
        );
    }

    /**
     * Updates the preference {@link Constants.Preferences.Keys#EXPORT_TIME_REG_FILE_NAME}.
     * @param ctx The context when updating the preference.
     * @param fileName The {@link String} to store in the preferences.
     */
    public static void setTimeRegistrationExportFileName(Context ctx, String fileName) {
        if(fileName == null) {
            return;
        }
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(Constants.Preferences.Keys.EXPORT_TIME_REG_FILE_NAME, fileName);
        editor.commit();
    }

    /**
     * Get the preference for key {@link Constants.Preferences.Keys#EXPROT_TIME_REG_FILE_TYPE}.
     * @param ctx The context when accessing the preference.
     * @return The {@link FileType} represented by the value in the preferences. If the value in the preferences
     * could not be matched on any instance of the enum it will return null.
     */
    public static FileType getTimeRegistrationExportFileType(Context ctx) {
        String extension = getSharedPreferences(ctx).getString(
                Constants.Preferences.Keys.EXPROT_TIME_REG_FILE_TYPE,
                FileType.TEXT.getExtension()
        );
        return FileType.matchFileType(extension);
    }

    /**
     * Updates the preference {@link Constants.Preferences.Keys#EXPROT_TIME_REG_FILE_TYPE}.
     * @param ctx The context when updating the preference.
     * @param fileType The {@link FileType} to store in the preferences.
     */
    public static void setTimeRegistrationExportFileType(Context ctx, FileType fileType) {
        if(fileType == null) {
            return;
        }
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(Constants.Preferences.Keys.EXPROT_TIME_REG_FILE_TYPE, fileType.getExtension());
        editor.commit();
    }

    /**
     * Get the preference for key {@link Constants.Preferences.Keys#EXPROT_TIME_REG_CSV_SEPARATOR}.
     * @param ctx The context when accessing the preference.
     * @return The {@link CsvSeparator} represented by the value in the preferences. If the value in the preferences
     * could not be matched on any instance of the enum it will return null.
     */
    public static CsvSeparator getTrimeRegistrationCsvSeparator(Context ctx) {
        String seperator = getSharedPreferences(ctx).getString(
                Constants.Preferences.Keys.EXPROT_TIME_REG_CSV_SEPARATOR,
                String.valueOf(CsvSeparator.SEMICOLON.getSeperator())
        );
        return CsvSeparator.matchFileType(seperator);
    }

    /**
     * Updates the preference {@link Constants.Preferences.Keys#EXPROT_TIME_REG_CSV_SEPARATOR}.
     * @param ctx The context when updating the preference.
     * @param separator The {@link CsvSeparator} to store in the preferences.
     */
    public static void setTrimeRegistrationCsvSeparator(Context ctx, CsvSeparator separator) {
        if(separator == null) {
            return;
        }
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(Constants.Preferences.Keys.EXPROT_TIME_REG_CSV_SEPARATOR, String.valueOf(separator.getSeperator()));
        editor.commit();
    }

    /**
     * Get the preference for key {@link Constants.Preferences.Keys#SELECTED_PROJECT_ID}. If no selected project id is
     * found the default value will be {@link Constants.Preferences#SELECTED_PROJECT_ID_DEFAULT_VALUE}.
     * @param ctx The context when getting the selected project id.
     * @return The unique identifier of a {@link eu.vranckaert.worktime.model.Project} if one is found. Otherwise the
     * default value {@link Constants.Preferences#SELECTED_PROJECT_ID_DEFAULT_VALUE}.
     */
    public static int getSelectedProjectId(Context ctx) {
        return getSharedPreferences(ctx).getInt(
                Constants.Preferences.Keys.SELECTED_PROJECT_ID,
                Constants.Preferences.SELECTED_PROJECT_ID_DEFAULT_VALUE
        );
    }

    /**
     * Updates the preference {@link Constants.Preferences.Keys#SELECTED_PROJECT_ID}.
     * @param ctx The context when updating the preference.
     * @param projectId The projectId to put in the preferences.
     * @param projectId The projectId to put in the preferences.
     */
    public static void setSelectedProjectId(Context ctx, int projectId) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(Constants.Preferences.Keys.SELECTED_PROJECT_ID, projectId);
        editor.commit();
    }
}
