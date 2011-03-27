package eu.vranckaert.worktime.dao;

import eu.vranckaert.worktime.dao.generic.GenericDao;
import eu.vranckaert.worktime.model.Project;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:56
 */
public interface ProjectDao extends GenericDao<Project, Integer> {
    /**
     * Checks if a certain name for a project is already in use.
     * @param projectName The name of the project to check for.
     * @return {@link Boolean#TRUE} if a project with this name already exists, {@link Boolean#FALSE} if not.
     */
    boolean isNameAlreadyUsed(String projectName);

    /**
     * Count the total number of projects available in the database.
     * @return The number of available projects.
     */
    int countTotalNumberOfProjects();

    /**
     * Find the default {@link Project}.
     * @return The default {@link Project}.
     */
    Project findDefaultProject();
}
