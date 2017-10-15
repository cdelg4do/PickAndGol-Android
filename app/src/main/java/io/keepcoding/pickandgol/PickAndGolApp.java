package io.keepcoding.pickandgol;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import io.keepcoding.pickandgol.interactor.DownloadCategoriesInteractor;
import io.keepcoding.pickandgol.manager.db.DBManagerBuilder;
import io.keepcoding.pickandgol.manager.db.DBManagerBuilder.DatabaseType;

import static io.keepcoding.pickandgol.manager.db.DBManagerBuilder.DatabaseType.REALM;


/**
 * This class maintains the global application state.
 */
public class PickAndGolApp extends Application {

    private final static String LOG_TAG = "APP";

    // App context: weak reference so that we avoid memory leaks when closing the app
    private static WeakReference<Context> appContext;

    // Values to init the local database (CHANGE AS NEEDED)
    public static final DatabaseType DBTYPE = REALM;

    // APPLICATION ENTRY POINT
    // (where all the app initial settings should be done)
    @Override
    public void onCreate() {
        super.onCreate();

        // Keep a copy of the application context
        appContext = new WeakReference<>( getApplicationContext() );

        // Set the type of database we will use, and its setup param (if needed)
        initDatabase(DBTYPE, getApplicationContext());

        // Download current event categories
        updateCategories();
    }

    // Override to warn about memory warnings
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // Get the app context
    public static Context getContext() {
        return appContext.get();
    }

    // Set the type of database we will use, and its setup param (if needed)
    private void initDatabase(DatabaseType dbType, @Nullable Object dbInitParam) {

        new DBManagerBuilder().type(dbType)
                              .init(dbInitParam);
    }

    // Update the local database with the current event categories from the server
    private void updateCategories() {

        Log.d(LOG_TAG, "Refreshing categories from server...");
        final Context ctx = getContext();

        new DownloadCategoriesInteractor().execute(ctx, false, new DownloadCategoriesInteractor.DownloadCategoriesInteractorListener() {

            @Override
            public void onDownloadCategoriesFail(Throwable e) {
                Log.e(LOG_TAG, "Unable to update categories from the server. Category lists may be outdated: ", e);
            }

            @Override
            public void onDownloadCategoriesSuccess(int count) {
                Log.d(LOG_TAG, "Categories successfully updated: "+ count);
            }
        });
    }

}
