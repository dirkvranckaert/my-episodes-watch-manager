package eu.vranckaert.episodeWatcher.twopointo.context;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import eu.vranckaert.episodeWatcher.twopointo.context.login.LoginActivity;

/**
 * Date: 03/11/15
 * Time: 21:43
 *
 * @author Dirk Vranckaert
 */
public class NavigationManager {
    public static void openUrl(Activity activity, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);
    }

    public static void startLogon(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void startApp(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void restartApplication(Activity activity) {
        Intent intent = new Intent(activity, StartupActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
