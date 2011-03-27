package eu.vranckaert.worktime.utils;

import android.content.Context;
import android.content.Intent;
import eu.vranckaert.worktime.activities.HomeActivity;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 04:40
 */
public class IntentUtil {
    public static void goHome(Context ctx) {
        Intent intent = new Intent(ctx, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
    }
}
