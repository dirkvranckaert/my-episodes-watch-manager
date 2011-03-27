package eu.vranckaert.worktime.service.impl;

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.dao.ProjectDao;
import eu.vranckaert.worktime.dao.TimeRegistrationDao;
import eu.vranckaert.worktime.dao.impl.ProjectDaoImpl;
import eu.vranckaert.worktime.dao.impl.TimeRegistrationDaoImpl;
import eu.vranckaert.worktime.exceptions.AtLeastOneProjectRequiredException;
import eu.vranckaert.worktime.exceptions.ProjectStillInUseException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.utils.preferences.Preferences;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 04:20
 */
public class ProjectServiceImpl implements ProjectService {
    private static final String LOG_TAG = ProjectServiceImpl.class.getSimpleName();

    @Inject
    private ProjectDao dao;

    @Inject
    private Context ctx;

    @Inject
    private TimeRegistrationDao timeRegistrationDao;

    /**
     * Enables the use of this service outside of RoboGuice!
     * @param ctx The context to insert
     */
    public ProjectServiceImpl(Context ctx) {
        this.ctx = ctx;
        dao = new ProjectDaoImpl(ctx);
        timeRegistrationDao = new TimeRegistrationDaoImpl(ctx);
    }

    /**
     * Default constructor required by RoboGuice!
     */
    public ProjectServiceImpl() {}

    /**
     * {@inheritDoc}
     */
    public Project save(Project project) {
        return dao.save(project);
    }

    /**
     * {@inheritDoc}
     */
    public List<Project> findAll() {
        return dao.findAll();
    }

    /**
     * {@inheritDoc}
     */
    public void remove(Project project, boolean force) throws ProjectStillInUseException,
                                                              AtLeastOneProjectRequiredException {
        if (countTotalNumberOfProjects() > 1) {
            List<TimeRegistration> timeRegistrations = timeRegistrationDao.findTimeRegistrationsForProject(project);
            Log.d(LOG_TAG, timeRegistrations.size() + " timeregistrations found coupled to the project to delete");
            if(!timeRegistrations.isEmpty() && !force) {
                throw new ProjectStillInUseException("The project is linked to existing time registrations!");
            } else if(force) {
                Log.d(LOG_TAG, "Forcing to delete all timeregistrations linked to the project first!");
                for (TimeRegistration treg : timeRegistrations) {
                    timeRegistrationDao.delete(treg);
                }
            }
            dao.delete(project);
            if (project.isDefaultValue()) {
                changeDefaultProjectUponProjectRemoval(project);
            }
            checkSelectedProjectUponProjectRemoval(project);
        } else {
            throw new AtLeastOneProjectRequiredException("At least on project is required so this project cannot be removed");
        }
    }

    /**
     * Change the default project upon removing a project which is set to be the default.
     * @param projectForRemoval The default project to be removed.
     */
    private void changeDefaultProjectUponProjectRemoval(Project projectForRemoval) {
        Log.d(LOG_TAG, "Trying to remove project " + projectForRemoval.getName() + " while it's a default project");
        if (!projectForRemoval.isDefaultValue()) {
            return;
        }

        List<Project> availableProjects = dao.findAll();
        availableProjects.remove(projectForRemoval);

        if (availableProjects.size() > 0) {
            Log.d(LOG_TAG, availableProjects.size() + " projects found to be the new default project");
            Project newDefaultProject = availableProjects.get(0);
            Log.d(LOG_TAG, "New default project is " + newDefaultProject.getName());
            newDefaultProject.setDefaultValue(true);
            dao.update(newDefaultProject);
        }
    }

    /**
     * Checks if the removed project was the selected project to link to new {@link TimeRegistration} instances. If so
     * set the default project as selected project.
     * @param project The project to check for.
     */
    private void checkSelectedProjectUponProjectRemoval(Project project) {
        int projectId = Preferences.getSelectedProjectId(ctx);
        if (project.getId() == projectId) {
            Preferences.setSelectedProjectId(ctx, dao.findDefaultProject().getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNameAlreadyUsed(String projectName) {
        return dao.isNameAlreadyUsed(projectName);
    }

    /**
     * {@inheritDoc}
     */
    public int countTotalNumberOfProjects() {
        return dao.countTotalNumberOfProjects();
    }

    /**
     * {@inheritDoc}
     */
    public Project getSelectedProject() {
        int projectId = Preferences.getSelectedProjectId(ctx);

        if (projectId == Constants.Preferences.SELECTED_PROJECT_ID_DEFAULT_VALUE) {
            Log.d(LOG_TAG, "No project is found yet. Get the default project.");
            Project project = dao.findDefaultProject();
            Log.d(LOG_TAG, "Set the default project in the preferences to be the selected project");
            Preferences.setSelectedProjectId(ctx, project.getId());
            return project;
        } else {
            Project project = dao.findById(projectId);
            Log.d(LOG_TAG, "The selected project has id " + project.getId() + " and name " + project.getName());
            return project;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void changeDefaultProject(Project newDefaultProject) {
        Project oldDefaultProject = dao.findDefaultProject();

        oldDefaultProject.setDefaultValue(false);
        newDefaultProject.setDefaultValue(true);

        dao.update(oldDefaultProject);
        dao.update(newDefaultProject);
    }
}
