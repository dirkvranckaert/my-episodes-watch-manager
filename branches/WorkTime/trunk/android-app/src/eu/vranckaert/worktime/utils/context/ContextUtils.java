package eu.vranckaert.worktime.utils.context;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Locale;

/**
 * Context utils.
 * @author Dirk Vranckaert
 */
public class ContextUtils {
    /**
     * Get the current user locale.
     * @param context The context on which to search for the locale.
     * @return The locale.
     */
    public static Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().locale;
    }

    /**
     * Hides the soft keyboard of the device.
     * @param context The context on which a keyboard is shown.
     * @param someEditText Some {@link EditText} instance available on the view on which the keyboard should be hidden.
     */
    public static void hideKeyboard(Context context, EditText someEditText) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(someEditText.getWindowToken(), 0);
    }

    /**
     * Checks if an SD card is available in the current device.
     * @return {@link Boolean#TRUE} if SD card is available, {@link Boolean#FALSE} if no SD card is available.
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Checks if data can be written to the SD card.
     * @return {@link Boolean#TRUE} if the SD card is writable. {@link Boolean#FALSE} if the SD card is not writable.
     */
    public static boolean isSdCardWritable() {
        return !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * A lookup method for the name of the current version (something like 1.0.3).
     * @param ctx The context.
     * @return The current version name.
     */
    public static String getCurrentApplicationVersionName(Context ctx) {
        String name = ctx.getPackageName();
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(name,0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "ERROR";
        }
    }

    /**
     * A lookup method for the code of the current version.
     * @param ctx The context.
     * @return The current version code.
     */
    public static int getCurrentApplicationVersionCode(Context ctx) {
        String name = ctx.getPackageName();
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(name,0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}
