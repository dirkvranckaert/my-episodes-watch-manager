package eu.vranckaert.episodeWatcher.utils;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author Dirk Vranckaert
 * Date: 24-apr-2010
 * Time: 0:41:33
 */
public final class DateUtil {
    private static final String LOG_TAG = DateUtil.class.getName();
    //http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html#rfc822timezone
    private static final String[] dateFormats = {"dd-MM-yyyy"};

    /**
     * Formats a given date in the {@link java.text.DateFormat#LONG} format.
     * @param date The date to format.
     * @param context The context in which the user locale can be found!
     * @return The date representation in a string.
     */
    public static final String formatDateLong(Date date, Context context) {
        Locale locale = getCurrentLocale(context);
        return formatDateLong(date, locale);
    }
    
    /**
     * Formats a given date in the {@link java.text.DateFormat#FULL} format.
     * @param date The date to format.
     * @param context The context in which the user locale can be found!
     * @return The date representation in a string.
     */
    public static final String formatDateFull(Date date, Context context) {
        Locale locale = getCurrentLocale(context);
        return formatDateFull(date, locale);
    }
    
    /**
     * Formats a given date in the {@link java.text.DateFormat#FULL} format.
     * @param date The date to format.
     * @param locale The locale to use to format the date.
     * @return The date representation in a string.
     */
    public static final String formatDateFull(Date date, Locale locale) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
        return dateFormat.format(date);
    }

    /**
     * Formats a given date in the {@link java.text.DateFormat#LONG} format.
     * @param date The date to format.
     * @param locale The locale to use to format the date.
     * @return The date representation in a string.
     */
    public static final String formatDateLong(Date date, Locale locale) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        return dateFormat.format(date);
    }

    /**
     * Formats a given date in the {@link java.text.DateFormat#LONG} format.
     * @param date The date to format.
     * @return The date representation in a string.
     */
    public static final String formatDateLong(Date date) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        return dateFormat.format(date);
    }

    /**
     * Get the current locale of the user.
     * @param context The context in which the locale can be found.
     * @return The user's locale.
     */
    private static Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().locale;
    }

    /**
     * Tries to convert a string to a date intance.
     * @param dateString The string to convert.
     * @return A date instance. Null if the date pattern could not be determined!
     */
    public static Date convertToDate(String dateString) {
        for(String dateFormat : dateFormats) {
            DateFormat format = new SimpleDateFormat(dateFormat);
            format.setLenient(false);
            try {
                Date date = format.parse(dateString);
                return date;
            } catch (ParseException e) {
                Log.d(LOG_TAG, "Dateformat " + dateFormat + " not valid for dateString " + dateString);
            }
        }
        return null;
    }
}