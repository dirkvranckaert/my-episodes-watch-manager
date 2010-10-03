package eu.vranckaert.episodeWatcher.utils;

import android.test.AndroidTestCase;
import android.util.Log;
import junit.framework.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dirk Vranckaert
 *         Date: 3-okt-2010
 *         Time: 14:56:27
 */
public class DateUtilTest extends AndroidTestCase {
    private static final String LOG_TAG = DateUtilTest.class.getSimpleName();

    public void testConvertToDateFailure() {
        String dateString = "23 1990 september";

        Date dateUtilResult = DateUtil.convertToDate(dateString);

        Assert.assertNull(dateUtilResult);
    }

    public void testConvertToDate_d_m_Y() {
        //PHP d-m-Y
        String dateString = "23-10-1990";
        String dateFormat = "dd-MM-yyyy";

        Date date = formatDate(dateString, dateFormat);

        Date dateUtilResult = DateUtil.convertToDate(dateString);

        Assert.assertEquals(date.getTime(), dateUtilResult.getTime());
    }

    private Date formatDate(String dateString, String dateFormat) {
        DateFormat format = new SimpleDateFormat(dateFormat);
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            Log.d(LOG_TAG, "Could not parse date " + dateString + " for format " + dateFormat);
        }
        return date;
    }
}