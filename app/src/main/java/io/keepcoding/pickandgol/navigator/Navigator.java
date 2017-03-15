package io.keepcoding.pickandgol.navigator;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.keepcoding.pickandgol.activity.EventSearchSettingsActivity;
import io.keepcoding.pickandgol.activity.MainActivity;
import io.keepcoding.pickandgol.activity.NewEventActivity;
import io.keepcoding.pickandgol.activity.SplashActivity;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.search.EventSearchParams;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * This class manages all the navigation between the activities of the application.
 * (this is an abstract class, all its methods are public and static)
 */
public class Navigator {

    public static final int EVENT_SEARCH_ACTIVITY_REQUEST_CODE = 1001;
    public static final int NEW_EVENT_ACTIVITY_REQUEST_CODE = 1002;

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

    /**
     * Navigates from an instance of MainActivity to another of EventSearchSettingsActivity,
     * passing a given EventSearchParams object under MainActivity.CURRENT_EVENT_SEARCH_PARAMS_KEY
     * (the MainActivity will wait for result EVENT_SEARCH_ACTIVITY_REQUEST_CODE)
     *
     * @param pubDetailActivity context for the intent created during the operation
     * @param currentPub        the Pub to associate the new Event to (in case of Event creation)
     * @return                  a reference to the intent created (useful for testing)
     */
    public static Intent fromPubDetailActivityToNewEventActivity(final MainActivity pubDetailActivity,
  //public static Intent fromPubDetailActivityToNewEventActivity(final PubDetailActivity pubDetailActivity,
                                                                 @NonNull Pub currentPub) {

        Intent i = new Intent(pubDetailActivity, NewEventActivity.class);
        i.putExtra(NewEventActivity.PUB_MODEL_KEY, currentPub);
        pubDetailActivity.startActivityForResult(i, NEW_EVENT_ACTIVITY_REQUEST_CODE);

        return i;
    }

    /**
     * Returns from an instance of NewEventActivity to the previous activity,
     * sending back a new Event object, if necessary.
     *
     * @param newEventActivity  context for the intent created during the operation
     * @param newEvent          the new event created in the activity (if not null)
     * @return                  a reference to the intent created (useful for testing)
     */
    public static Intent backFromNewEventActivity(final NewEventActivity newEventActivity,
                                                  @Nullable Event newEvent) {

        Intent i = new Intent();

        if (newEvent != null) {

            i.putExtra(NewEventActivity.NEW_EVENT_KEY, newEvent);
            newEventActivity.setResult(RESULT_OK, i);
        }
        else
            newEventActivity.setResult(RESULT_CANCELED, i); // The operation failed or was canceled

        newEventActivity.finish();
        return i;
    }
}
