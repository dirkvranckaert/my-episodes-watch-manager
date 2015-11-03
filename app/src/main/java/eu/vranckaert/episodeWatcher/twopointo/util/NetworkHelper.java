package eu.vranckaert.episodeWatcher.twopointo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Date: 07/05/14
 * Time: 13:44
 *
 * @author Dirk Vranckaert
 */
public class NetworkHelper {
    private static final String LOG_TAG = NetworkHelper.class.getSimpleName();

    /**
     * Checks if the device is connected with internet or not.
     * @param ctx The app-context.
     * @return {@link Boolean#TRUE} if the device is connected or connecting, {@link Boolean#FALSE} if no connection is
     * available.
     */
    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.d(LOG_TAG, "Device is online");
            return true;
        }
        Log.d(LOG_TAG, "Device is not online");
        return false;
    }

    /**
     * Checks if the device is connected to a WiFi network or not.
     * @param ctx The app-context.
     * @return {@link Boolean#TRUE} if the device is connected to a WiFi network, {@link Boolean#FALSE} otherwise.
     */
    public static boolean isConnectedToWifi(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            SupplicantState state = wifiManager.getConnectionInfo().getSupplicantState();
            if (state != null) {
                NetworkInfo.DetailedState detailedState = WifiInfo.getDetailedStateOf(state);
                if (detailedState != null) {
                    if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
