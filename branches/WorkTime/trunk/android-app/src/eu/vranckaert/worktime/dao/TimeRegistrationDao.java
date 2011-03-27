package eu.vranckaert.worktime.dao;

import eu.vranckaert.worktime.dao.generic.GenericDao;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 17:31
 */
public interface TimeRegistrationDao extends GenericDao<TimeRegistration, Integer> {
    /**
     * Count the total number of time registrations available in the database.
     * @return The number of available time registrations.
     */
    int countTotalNumberOfTimeRegistrations();

    /**
     * Find the latest time registration. Returns <b>null</b> if no time regstrations are found!
     * @return The latest time registration.
     */
    TimeRegistration getLatestTimeRegistration();

    /**
     * Find all time registrations bound to one specific project.
     * @param project The project.
     * @return The time registrations for that project.
     */
    List<TimeRegistration> findTimeRegistrationsForProject(Project project);
}
