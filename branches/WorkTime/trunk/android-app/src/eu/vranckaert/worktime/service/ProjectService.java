package eu.vranckaert.worktime.service;

import eu.vranckaert.worktime.exceptions.AtLeastOneProjectRequiredException;
import eu.vranckaert.worktime.exceptions.ProjectStillInUseException;
import eu.vranckaert.worktime.model.Project;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 04:19
 */
public interface ProjectService {
    /**
     * Persist a new project instance.
     * @param project The {@link eu.vranckaert.worktime.model.Project} instance to persist.
     * @return The persisted instance.
     */
    Project save(Project project);

    /**
     * Find all persisted projects.
     * @return All projects.
     */
    List<Project> findAll();

    /**
     * Remove a project.
     * @param project The project to remove.
     * @param force If set to {@link Boolean#TRUE} all {@link eu.vranckaert.worktime.model.TimeRegistration} instances
     * linked to the project will be deleted first, then the project. If set to {@link Boolean#FALSE} nothing will
     * happen.
     * @throws ProjectStillInUseException If the project is coupled to time registrations and the force-option is not
     * used this exception is thrown.
     * @throws AtLeastOneProjectRequiredException If this project is the last project available this exception is
     * thrown.
     */
    void remove(Project project, boolean force) throws ProjectStillInUseException, AtLeastOneProjectRequiredException;

    /**
     * Checks if a certain name for a project is already in use.
     * @param projectName The name of the project to check for.
     * @return {@link Boolean#TRUE} if a project with this name already exists, {@link Boolean#FALSE} if not.
     */
    boolean isNameAlreadyUsed(String projectName);

    /**
     * Check how many projects are available in the DB.
     * @return The number of projects found.
     */
    int countTotalNumberOfProjects();

    /**
     * Retrieve the selected project to be displayed in the widget and to which new
     * {@link eu.vranckaert.worktime.model.TimeRegistration} instances will be linked to.
     * @return The selected project. If no selected project is found the default project is used as the selected one.
     */
    Project getSelectedProject();

    /**
     * Change the default {@link Project} and make the provided one the the default.
     * @param newDefaultProject The {@link Project} instance which should be made default now.
     */
    void changeDefaultProject(Project newDefaultProject);
}
