package eu.vranckaert.worktime.dao.utils;

/**
 * User: DIRK VRANCKAERT
 * Date: 09/02/11
 * Time: 19:28
 */
public interface ResultMapper<T> {
    T mapResult(String [] rs);
}