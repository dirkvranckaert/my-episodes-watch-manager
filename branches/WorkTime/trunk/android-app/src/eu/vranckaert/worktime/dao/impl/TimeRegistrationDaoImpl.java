package eu.vranckaert.worktime.dao.impl;

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RawResults;
import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.stmt.StatementBuilder;
import eu.vranckaert.worktime.comparators.TimeRegistrationDescendingByStartdate;
import eu.vranckaert.worktime.dao.TimeRegistrationDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 17:31
 */
public class TimeRegistrationDaoImpl extends GenericDaoImpl<TimeRegistration, Integer> implements TimeRegistrationDao{
    private static final String LOG_TAG = TimeRegistrationDaoImpl.class.getSimpleName();

    @Inject
    public TimeRegistrationDaoImpl(final Context context) {
        super(TimeRegistration.class, context);
    }

    public void assembleModel(TimeRegistration entity) {}

    public int countTotalNumberOfTimeRegistrations() {
        int numRecs = 0;
        try {
            RawResults result = dao.queryForAllRaw("select count(*) from timeregistration");
            Log.d(LOG_TAG, result.getNumberColumns() + " number of columns found!");
            CloseableIterator<String[]> iterator = result.iterator();
            while(iterator.hasNext()) {
                String[] values = iterator.next();
                numRecs = Integer.parseInt(values[0]);
            }
        } catch (SQLException e) {
            throwFatalException(e);
        }
        return numRecs;
    }

    /**
     * {@inheritDoc}
     */
    public TimeRegistration getLatestTimeRegistration() {
        List<TimeRegistration> timeRegistrations = findAll();
        if(timeRegistrations.size() > 0) {
            Collections.sort(timeRegistrations, new TimeRegistrationDescendingByStartdate());
            return timeRegistrations.get(0);
        } else {
            return null;
        }
    }

    public List<TimeRegistration> findTimeRegistrationsForProject(Project project) {
        StatementBuilder<TimeRegistration,Integer> sb = dao.statementBuilder();
        try {
            sb.where().eq("projectId", project.getId());
            PreparedStmt<TimeRegistration> ps = sb.prepareStatement();
            return dao.query(ps);
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Could not execute the query... Returning null");
            return null;
        }
    }
}
