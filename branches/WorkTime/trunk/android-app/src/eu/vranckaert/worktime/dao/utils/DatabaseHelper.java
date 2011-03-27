package eu.vranckaert.worktime.dao.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import eu.vranckaert.worktime.R;

import java.sql.SQLException;

/**
 * A utility class to be used to setup and interact with a database.
 * @param <T> Entity.
 * @param <ID> ID type.
 */
public class DatabaseHelper<T, ID> extends OrmLiteSqliteOpenHelper {
    /**
     * Logging
     */
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    /**
     * The database type.
     */
    private DatabaseType databaseType = new SqliteAndroidDatabaseType();

    /**
     * The context.
     */
    private Context context = null;

    /**
     * Create a new database helper.
     * @param context The context.
     * @param database The database name.
     * @param version The version of the database.
     */
    public DatabaseHelper(Context context, String database, int version) {
        super(context, database, null, version);
        this.context = context;
        Log.i(LOG_TAG, "Creating database, databasename = " + database + ", version = " + version);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.d(LOG_TAG, "Creating the database");
            for(Tables table : Tables.values()) {
                TableUtils.createTable(databaseType, connectionSource, table.getTableClass());
            }
            ContentValues values = new ContentValues();
            values.put("name", context.getString(R.string.default_project_name));
            values.put("comment", context.getString(R.string.default_project_comment));
            values.put("defaultValue", true);
            database.insert("project", null, values);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Excpetion while creating the database", e);
            throw new RuntimeException("Excpetion while creating the database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.d(LOG_TAG, "Updating the database");
            for(Tables table : Tables.values()) {
                TableUtils.dropTable(databaseType, connectionSource, table.getTableClass(), true);
            }
            onCreate(database);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Excpetion while updating the database from version " + oldVersion + "to " + newVersion, e);
            throw new RuntimeException("Excpetion while updating the database from version " + oldVersion + "to " + newVersion, e);
        }
    }

    @Override
    public void close() {
        Log.d(LOG_TAG, "Closing connection");
        super.close();
    }

    /**
     * Retrieve a DAO object.
     * @param clazz The entity-class for which a DAO object must be retrieved.
     * @return The DAO-instance.
     */
    public Dao<T, ID> getDao(java.lang.Class<T> clazz) {
        Dao<T, ID> dao = null;
        try {
            Log.d(LOG_TAG, "Creation of DAO for class " + clazz.getSimpleName());
            dao = BaseDaoImpl.createDao(databaseType, getConnectionSource(), clazz);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Creation of dao failed for " + clazz.getSimpleName());
            throw new RuntimeException("Creation of dao failed for " + clazz.getSimpleName(), e);
        }
        return dao;
    }
}
