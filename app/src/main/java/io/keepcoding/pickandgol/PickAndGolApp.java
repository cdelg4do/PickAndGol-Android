package io.keepcoding.pickandgol;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;

import io.keepcoding.pickandgol.interactor.DownloadCategoriesInteractor;
import io.realm.Realm;


/**
 * This class maintains the global application state.
 */
public class PickAndGolApp extends Application {

    private final static String LOG_TAG = "APP";

    // App context: weak reference so that we avoid memory leaks when closing the app
    private static WeakReference<Context> appContext;

    // Entry point of the app
    // (app's initial settings can be done here)
    @Override
    public void onCreate() {
        super.onCreate();

        // Keep a copy of the application context
        appContext = new WeakReference<>( getApplicationContext() );

        // Init Realm
        Realm.init( getApplicationContext() );

        // Download current event categories
        Log.d(LOG_TAG, "Refreshing categories from server...");
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

    // Update the local database with the current event categories from the server
    private void updateCategories() {

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
