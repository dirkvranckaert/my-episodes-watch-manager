package eu.vranckaert.worktime.utils.date;

import android.content.Context;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Date utils.
 * @author Dirk Vranckaert
 */
public class DateUtils {
    private static final String LOG_TAG = DateUtils.class.getSimpleName();

    /**
     * Converts a certain date to a date-string based on the users locale in the context.
     * @param date The date to convert.
     * @param format The date format to use.
     * @param context The context in which the locale is stored.
     * @return The formatted string.
     */
    public static final String convertDateToString(Date date, DateTimeFormats format, Context context) {
        Locale locale = ContextUtils.getCurrentLocale(context);
        return convertDateToString(date, format, locale);
    }

    /**
     * Converts a certain date to a date-string based on the users locale.
     * @param date The date to convert.
     * @param format The date format to use.
     * @param locale The users locale.
     * @return The formatted string.
     */
    public static final String convertDateToString(Date date, DateTimeFormats format, Locale locale) {
        DateFormat dateFormat = DateFormat.getDateInstance(format.getStyle(), locale);
        return dateFormat.format(date);
    }

    /**
     * Converts a certain date to a time-string based on the users locale in the context.
     * @param date The date to convert.
     * @param format The date format to use.
     * @param context The context in which the locale is stored.
     * @return The formatted string.
     */
    public static final String convertTimeToString(Date date, DateTimeFormats format, Context context) {
        Locale locale = ContextUtils.getCurrentLocale(context);
        return convertTimeToString(date, format, locale);
    }

    /**
     * Converts a certain date to a time-string based on the users locale.
     * @param date The date to convert.
     * @param format The date format to use.
     * @param locale The users locale.
     * @return The formatted string.
     */
    public static final String convertTimeToString(Date date, DateTimeFormats format, Locale locale) {
        DateFormat dateFormat = DateFormat.getTimeInstance(format.getStyle(), locale);
        return dateFormat.format(date);
    }

    /**
     * Converts a date to an entire date-time-string based on the users locale in the context.
     * @param date The date to convert.
     * @param format The date format to use.
     * @param context The context in which the locale is stored.
     * @return The formatted string.
     */
    public static final String convertDateTimeToString(Date date, DateTimeFormats format, Context context) {
        Locale locale = ContextUtils.getCurrentLocale(context);
        return convertDateTimeToString(date, format, locale);
    }

    /**
     * Converts a date to an entire date-time-string based on the users locale.
     * @param date The date to convert.
     * @param format The date format to use.
     * @param locale The users locale.
     * @return The formatted string.
     */
    public static final String convertDateTimeToString(Date date, DateTimeFormats format, Locale locale) {
        int style = format.getStyle();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(style, style, locale);
        return dateFormat.format(date);
    }

    /**
     * Calculates the time ({@link Period}) between two dates. If the startDate is not before the endDate the dates
     * will be swapped.
     * @param startDate The start date for the period.
     * @param endDate The ending date for the period.
     * @return The {@link Period} between the two dates. The period is calculated in hours, minutes, seconds and millis.
     */
    public static final Period calculatePeriod(Date startDate, Date endDate) {
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        end.set(Calendar.MILLISECOND, 0);

        if(end.before(start)) {
            Calendar swap = start;
            start = end;
            end = swap;
        }

        Interval interval = new Interval(start.getTime().getTime(), end.getTime().getTime());

        Period period = interval.toPeriod(PeriodType.time());

        return period;
    }
}
