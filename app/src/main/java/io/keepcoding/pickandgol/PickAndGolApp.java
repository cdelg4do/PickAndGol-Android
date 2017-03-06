package io.keepcoding.pickandgol;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

import io.realm.Realm;


/**
 * This class maintains the global application state.
 */
public class PickAndGolApp extends Application {

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
}
