package eu.vranckaert.episodeWatcher.utils;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author Dirk Vranckaert
 * Date: 24-apr-2010
 * Time: 0:41:33
 */
public final class DateUtil {
    private static final String LOG_TAG = DateUtil.class.getName();
    private static final String DATE_FORMAT = "d/m/yy";

    public static final String formatDateLong(Date date, Context context) {
        Locale locale = getCurrentLocale(context);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        return dateFormat.format(date);
    }

    private static Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().locale;
    }
}