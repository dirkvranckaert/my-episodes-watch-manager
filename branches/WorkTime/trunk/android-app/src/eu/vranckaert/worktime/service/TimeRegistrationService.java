package eu.vranckaert.worktime.service;

import android.content.Context;
import eu.vranckaert.worktime.enums.export.ExportType;
import eu.vranckaert.worktime.model.TimeRegistration;

import java.io.File;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/02/11
 * Time: 00:13
 */
public interface TimeRegistrationService {
    /**
     * Find all persisted time registrations.
     * @return All time registrations.
     */
    List<TimeRegistration> findAll();

    /**
     * Create a new instance of {@link TimeRegistration}.
     * @param timeRegistration The instance to create.
     */
    void create(TimeRegistration timeRegistration);

    /**
     * Update a time registration instance.
     * @param timeRegistration The instance to update.
     */
    void update(TimeRegistration timeRegistration);

    /**
     * Find the latest time registration. Returns <b>null</b> if no time regstrations are found!
     * @return The latest time registration.
     */
    TimeRegistration getLatestTimeRegistration();

    /**
     * Export all time registrations.
     * @param exportType The type of export.
     * @param ctx The context.
     * @return The exported file.
     */
    File export(final ExportType exportType, final Context ctx);

    /**
     * Removes an existing timeregistration.
     *
     * @param timeRegistration The registration to remove.
     */
    void remove(TimeRegistration timeRegistration);
}
