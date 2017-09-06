package io.keepcoding.pickandgol.manager.db;

import android.content.Context;
import android.support.annotation.Nullable;

import io.keepcoding.pickandgol.manager.db.realm.RealmDBManager;
import io.realm.Realm;

import static io.keepcoding.pickandgol.manager.db.DBManagerBuilder.DatabaseType.*;


/**
 * This class lets to init/access a specific implementation of DBManager using the Builder pattern.
 *
 * Usages:
 *      DBManagerBuilder dbBuilder = new DBManagerBuilder();
 *      dbBuilder.init(...)
 *      DBManager mgr1 = new DBManagerBuilder().build();
 *
 *      DBManager mgr2 =  new DBManagerBuilder().type(REALM).build();
 *
 * ToDo: set the appropriate behavior in build() and init() when adding more implementation types
 */
public class DBManagerBuilder {

    // Current implementations of DBManager (add more when needed)
    public enum DatabaseType {

        DEFAULT,    // The default value if no other is specified - Do Not Remove
        REALM       // Implementation with Realm
    }


    // Indicates the type of DBManager implementation we use
    private DatabaseType type;


    // Class constructor (sets the default implementation type)
    public DBManagerBuilder() {
        type = DEFAULT;
    }

    // Sets a specific implementation type for the builder
    public DBManagerBuilder type(DatabaseType type) {

        this.type = type;
        return this;
    }

    // Does the initial setup before using the database
    // (implement at will for each implementation, if needed)
    public DBManagerBuilder init(@Nullable Object param) {

        switch (type) {

            case REALM:
            default:
                if (param != null && param instanceof Context) {
                    Context ctx = (Context) param;
                    Realm.init(ctx);
                }
        }

        return this;
    }

    // Actually returns an instance of DBManager
    public DBManager build() {

        switch (type) {

            case REALM:
            default:
                return RealmDBManager.getDBManager();
        }
    }
}
