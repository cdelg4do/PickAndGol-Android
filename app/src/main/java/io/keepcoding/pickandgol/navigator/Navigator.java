package io.keepcoding.pickandgol.navigator;

import android.content.Intent;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.activity.EventSearchSettingsActivity;
import io.keepcoding.pickandgol.activity.MainActivity;
import io.keepcoding.pickandgol.activity.SplashActivity;
import io.keepcoding.pickandgol.search.EventSearchParams;

import static android.app.Activity.RESULT_OK;

/**
 * This class manages all the navigation between the activities of the application.
 * (this is an abstract class, all its methods are public and static)
 */
public class Navigator {

    public static final int EVENT_SEARCH_ACTIVITY_REQUEST_CODE = 1001;

    /**
     * Navigates from an instance of SplashActivity to another of MainActivity
     * (finishes the SplashActivity so that it cant be accessed again by clicking the back button)
     *
     * @param splashActivity    context for the intent created during the operation
     * @return                  a reference to the intent created (useful for testing)
     */
    public static Intent fromSplashActivityToMainActivity(final SplashActivity splashActivity) {

        final Intent i = new Intent(splashActivity, MainActivity.class);
        splashActivity.startActivity(i);
        splashActivity.finish();

        return i;
    }

    /**
     * Navigates from an instance of MainActivity to another of EventSearchSettingsActivity,
     * passing a given EventSearchParams object under MainActivity.CURRENT_EVENT_SEARCH_PARAMS_KEY
     * (the MainActivity will wait for result EVENT_SEARCH_ACTIVITY_REQUEST_CODE)
     *
     * @param mainActivity  context for the intent created during the operation
     * @return              a reference to the intent created (useful for testing)
     */
    public static Intent fromMainActivityToEventSearchActivity(final MainActivity mainActivity,
                                                               @NonNull EventSearchParams currentSearchParams,
                                                               boolean showDistanceSelector) {

        Intent i = new Intent(mainActivity, EventSearchSettingsActivity.class);
        i.putExtra(MainActivity.CURRENT_EVENT_SEARCH_PARAMS_KEY, currentSearchParams);
        i.putExtra(MainActivity.SHOW_DISTANCE_SELECTOR_KEY, showDistanceSelector);
        mainActivity.startActivityForResult(i, EVENT_SEARCH_ACTIVITY_REQUEST_CODE);

        return i;
    }

    /**
     * Returns from an instance of EventSearchSettingsActivity to the previous activity,
     * sending back a new EventSearchParams object.
     *
     * @param eventSearchSettingsActivity   context for the intent created during the operation
     * @param newSearchParams       the new search settings that has been configured on the Activity
     * @return                      a reference to the intent created (useful for testing)
     */
    public static Intent backFromEventSearchActivity(final EventSearchSettingsActivity eventSearchSettingsActivity,
                                                     @NonNull EventSearchParams newSearchParams) {

        Intent i = new Intent();
        i.putExtra(MainActivity.NEW_EVENT_SEARCH_PARAMS_KEY, newSearchParams);
        eventSearchSettingsActivity.setResult(RESULT_OK, i);
        eventSearchSettingsActivity.finish();

        return i;
    }
}
