package eu.vranckaert.worktime.comparators;

import eu.vranckaert.worktime.model.Project;

import java.util.Comparator;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 17:12
 */
public class ProjectByNameComparator implements Comparator<Project> {
    public int compare(Project project1, Project project2) {
        return project1.getName().compareTo(project2.getName());
    }
}
