package eu.vranckaert.worktime.exceptions;

/**
 * User: DIRK VRANCKAERT
 * Date: 11/02/11
 * Time: 00:38
 */
public class ProjectStillInUseException extends Exception {
    public ProjectStillInUseException(String s) {
        super(s);
    }
}
