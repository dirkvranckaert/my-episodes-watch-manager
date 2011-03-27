package eu.vranckaert.worktime.dao.impl;

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RawResults;
import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.stmt.StatementBuilder;
import eu.vranckaert.worktime.dao.ProjectDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.exceptions.CorruptProjectDataException;
import eu.vranckaert.worktime.model.Project;

import java.sql.SQLException;
import java.util.List;


public class ProjectDaoImpl extends GenericDaoImpl<Project, Integer> implements ProjectDao {
    private static final String LOG_TAG = ProjectDaoImpl.class.getSimpleName();

    @Inject
    public ProjectDaoImpl(final Context context) {
        super(Project.class, context);
    }

    public void assembleModel(Project entity) {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNameAlreadyUsed(String projectName) {
        List<Project> projects = null;
        StatementBuilder<Project, Integer> sb = dao.statementBuilder();
        try {
            sb.where().eq("name", projectName);
            PreparedStmt<Project> ps = sb.prepareStatement();
            projects = dao.query(ps);
        } catch (SQLException e) {
            Log.d(LOG_TAG, "Could not execute the query... Returning false");
            return false;
        }

        if(projects == null || projects.size() == 0) {
            Log.d(LOG_TAG, "The name is not yet used!");
            return false;
        }
        Log.d(LOG_TAG, "The name is already in use!");
        return true;
    }

    public int countTotalNumberOfProjects() {
        int numRecs = 0;
        try {
            RawResults result = dao.queryForAllRaw("select count(*) from project");
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

    public Project findDefaultProject() {
        List<Project> projects = null;

        projects = findAll();
        Log.d(LOG_TAG, "Before getting default project, number of projects in DB is: " + projects.size());

        projects = null;

        StatementBuilder<Project, Integer> sb = dao.statementBuilder();
        try {
            sb.where().eq("defaultValue", 1);
            PreparedStmt<Project> ps = sb.prepareStatement();
            projects = dao.query(ps);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Could not execute the query... Returning null.", e);
            return null;
        }

        if(projects == null || projects.size() == 0 || projects.size() > 1) {
            String message = null;
            if (projects == null || projects.size() == 0) {
                message = "The project data is corrupt. No default project is found!";
            } else {
                message = "The project data is corrupt. More than one default project is found in the database!";
            }
            Log.e(LOG_TAG, message);
            throw new CorruptProjectDataException(message);
        } else {
            return projects.get(0);
        }
    }
}
