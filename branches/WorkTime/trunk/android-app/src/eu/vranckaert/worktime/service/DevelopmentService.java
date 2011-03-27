package eu.vranckaert.worktime.service;

import android.content.Context;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:54
 */
public interface DevelopmentService {
    void createSampleData(Context ctx);
    void clearDatabaseData(Context ctx);
    void bootCheck();
}
