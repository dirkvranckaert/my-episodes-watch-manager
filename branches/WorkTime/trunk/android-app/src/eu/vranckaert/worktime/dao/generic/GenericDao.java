package eu.vranckaert.worktime.dao.generic;

import eu.vranckaert.worktime.dao.utils.ResultMapper;

import java.util.Collection;
import java.util.List;

public interface GenericDao<T, ID> {
    /**
     * Find an entity by it identifier.
     * @param id The identifier.
     * @return The entity.
     */
    T findById(ID id);

    /**
     * Find all entities of one type.
     * @return A list of entities.
     */
    List<T> findAll();

    /**
     * Persists a new entity in the datbase or updates an already existing one.
     * @param entity The entity to store or update.
     * @return The stored entity.
     */
    T save(T entity);

    /**
     * Persists a new entity in the datbase or updates an already existing one.
     * @param entity The entity to store or update.
     * @return The stored entity.
     */
    T update(T entity);

    /**
     * Removes an entity.
     * @param entity The entity to remove.
     */
    void delete(T entity);

    /**
     * Refreshes the content of an object based on it's identifier.
     * @param entity The entity to refresh.
     * @return The number of entities refreshed. If everything is ok this should always be one!
     */
    int refresh(T entity);

    /**
     * Execute a raw query string and maps the result to object.
     * @param sqlQuery The raw query string
     * @param mapper The mapper implementation.
     * @return A collection of the mapped entities.
     */
    Collection<T> queryForAllRaw(String sqlQuery, ResultMapper<T> mapper);

    void assembleModel(T entity);
}
