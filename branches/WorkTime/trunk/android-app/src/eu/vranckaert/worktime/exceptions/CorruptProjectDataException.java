package eu.vranckaert.worktime.exceptions;

/**
 * A runtime exception if the project data found in the local database is corrupt.
 *
 * User: DIRK VRANCKAERT
 * Date: 02/03/11
 * Time: 00:08
 */
public class CorruptProjectDataException extends RuntimeException {
    public CorruptProjectDataException(String s) {
        super(s);
    }
}
