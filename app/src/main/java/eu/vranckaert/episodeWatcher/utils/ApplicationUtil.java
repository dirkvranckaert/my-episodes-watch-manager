package eu.vranckaert.episodeWatcher.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by IntelliJ IDEA.
 * User: dirk
 * Date: Nov 5, 2010
 * Time: 10:46:45 AM
 */
public class ApplicationUtil {
    public static String getCurrentApplicationVersion(Context ctx) {
        String name = ctx.getPackageName();
        String version = "";
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(name,0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {}
        return version;
    }
}
