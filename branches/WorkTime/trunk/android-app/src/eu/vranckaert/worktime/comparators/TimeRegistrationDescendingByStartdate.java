package eu.vranckaert.worktime.comparators;

import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.Comparator;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/02/11
 * Time: 01:02
 */
public class TimeRegistrationDescendingByStartdate implements Comparator<TimeRegistration> {
    public int compare(TimeRegistration timeRegistration1, TimeRegistration timeRegistration2) {
        return timeRegistration2.getStartTime().compareTo(timeRegistration1.getStartTime());
    }
}
