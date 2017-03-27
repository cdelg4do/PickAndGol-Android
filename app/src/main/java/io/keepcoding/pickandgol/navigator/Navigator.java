package io.keepcoding.pickandgol.navigator;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.keepcoding.pickandgol.activity.EditUserActivity;
import io.keepcoding.pickandgol.activity.EventDetailActivity;
import io.keepcoding.pickandgol.activity.EventSearchSettingsActivity;
import io.keepcoding.pickandgol.activity.LocationPickerActivity;
import io.keepcoding.pickandgol.activity.MainActivity;
import io.keepcoding.pickandgol.activity.NewEventActivity;
import io.keepcoding.pickandgol.activity.NewPubActivity;
import io.keepcoding.pickandgol.activity.NewUserActivity;
import io.keepcoding.pickandgol.activity.PubSearchSettingsActivity;
import io.keepcoding.pickandgol.activity.SplashActivity;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.search.EventSearchParams;
import io.keepcoding.pickandgol.search.PubSearchParams;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.INITIAL_LOCATION_LATITUDE_KEY;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.INITIAL_LOCATION_LONGITUDE_KEY;

/**
 * This class manages all the navigation between the activities of the application.
 * (this is an abstract class, all its methods are public and static)
 */
public class Navigator {

    public static final int EVENT_SEARCH_ACTIVITY_REQUEST_CODE = 1001;
    private static final int NEW_EVENT_ACTIVITY_REQUEST_CODE = 1002;
    public static final int PUB_SEARCH_ACTIVITY_REQUEST_CODE = 1003;
    private static final int NEW_PUB_ACTIVITY_REQUEST_CODE = 1004;
    public static final int LOCATION_PICKER_ACTIVITY_REQUEST_CODE = 1005;
    private static final int EDIT_EVENT_ACTIVITY_REQUEST_CODE = 1006;
    public static final int EDIT_USER_ACTIVITY_REQUEST_CODE = 1007;

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
     * Navigates from an instance of PubDetailActivity to another of NewEventActivity,
     * passing a given Pub object under NewEventActivity.PUB_MODEL_KEY
     * (the PubDetailActivity will wait for result NEW_EVENT_ACTIVITY_REQUEST_CODE)
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

    /**
     * Navigates from an instance of MainActivity to another of NewPubActivity
     * (the MainActivity will wait for result NEW_PUB_ACTIVITY_REQUEST_CODE)
     *
     * @param mainActivity  context for the intent created during the operation
     * @return              a reference to the intent created (useful for testing)
     */
    public static Intent fromMainActivityToNewPubActivity(final MainActivity mainActivity) {

        Intent i = new Intent(mainActivity, NewPubActivity.class);
        mainActivity.startActivityForResult(i, NEW_PUB_ACTIVITY_REQUEST_CODE);

        return i;
    }

    /**
     * Returns from an instance of NewPubActivity to the previous activity,
     * sending back a new Pub object, if necessary.
     *
     * @param newPubActivity  context for the intent created during the operation
     * @param newPub          the new pub created in the activity (if not null)
     * @return                a reference to the intent created (useful for testing)
     */
    public static Intent backFromNewPubActivity(final NewPubActivity newPubActivity,
                                                @Nullable Pub newPub) {

        Intent i = new Intent();

        if (newPub != null) {

            i.putExtra(NewPubActivity.NEW_PUB_KEY, newPub);
            newPubActivity.setResult(RESULT_OK, i);
        }
        else
            newPubActivity.setResult(RESULT_CANCELED, i); // The operation failed or was canceled

        newPubActivity.finish();
        return i;
    }

    /**
     * Navigates from an instance of NewPubActivity to another of LocationPickerActivity
     * (the NewPubActivity will wait for result LOCATION_PICKER_ACTIVITY_REQUEST_CODE)
     *
     * @param newPubActivity    context for the intent created during the operation
     * @param initialLatitude   initial latitude to center the picker map
     * @param initialLongitude  initial longitude to center the picker map
     * @return                  a reference to the intent created (useful for testing)
     */
    public static Intent fromNewPubActivityToLocationPickerActivity(final NewPubActivity newPubActivity,
                                                                    @Nullable Double initialLatitude,
                                                                    @Nullable Double initialLongitude) {

        Intent i = new Intent(newPubActivity, LocationPickerActivity.class);

        if (initialLatitude != null && initialLongitude != null) {
            i.putExtra(INITIAL_LOCATION_LATITUDE_KEY, initialLatitude);
            i.putExtra(INITIAL_LOCATION_LONGITUDE_KEY, initialLongitude);
        }

        newPubActivity.startActivityForResult(i, LOCATION_PICKER_ACTIVITY_REQUEST_CODE);
        return i;
    }

    /**
     * Returns from an instance of LocationPickerActivity to the previous activity,
     * sending back a pair of location coordinates, if a location was selected.
     *
     * @param locationPickerActivity    context for the intent created during the operation
     * @param selectedLatitude          initial latitude to center the picker map
     * @param selectedLongitude         initial longitude to center the picker map
     * @return                          a reference to the intent created (useful for testing)
     */
    public static Intent backFromLocationPickerActivity(final LocationPickerActivity locationPickerActivity,
                                                @Nullable Double selectedLatitude,
                                                @Nullable Double selectedLongitude) {

        Intent i = new Intent();

        boolean locationWasSelected = (selectedLatitude != null && selectedLongitude != null);

        if (locationWasSelected) {
            i.putExtra(LocationPickerActivity.SELECTED_LATITUDE_KEY, selectedLatitude);
            i.putExtra(LocationPickerActivity.SELECTED_LONGITUDE_KEY, selectedLongitude);
            locationPickerActivity.setResult(RESULT_OK, i);
        }
        else
            locationPickerActivity.setResult(RESULT_CANCELED, i); // The operation failed or was canceled

        locationPickerActivity.finish();
        return i;
    }

    /**
     * Navigates from an instance of MainActivity to another of EventDetailActivity,
     * passing the Event object to show under EventDetailActivity.EVENT_MODEL_KEY.
     *
     * @param mainActivity  context for the intent created during the operation
     * @param event         event to show in the new activity
     * @return              a reference to the intent created (useful for testing)
     */
    public static Intent fromMainActivityToEventDetailActivity(final MainActivity mainActivity,
                                                               @NonNull Event event) {

        Intent i = new Intent(mainActivity, EventDetailActivity.class);
        i.putExtra(EventDetailActivity.EVENT_MODEL_KEY, event);
        mainActivity.startActivity(i);

        return i;
    }

    public static Intent fromMainActivityToEditUserActivity(final MainActivity mainActivity) {
        Intent i = new Intent(mainActivity, EditUserActivity.class);
        mainActivity.startActivityForResult(i, EDIT_USER_ACTIVITY_REQUEST_CODE);

        return i;
    }

    public static Intent backFromEditUserActivity(final EditUserActivity userActivity, final User userModified) {
        Intent i = new Intent();
        if (userModified != null) {
            i.putExtra(EditUserActivity.SAVED_USER_KEY, userModified);
            userActivity.setResult(RESULT_OK, i);
        } else {
            userActivity.setResult(RESULT_CANCELED);
        }

        userActivity.finish();

        return i;
    }

    /**
     * Navigates from an instance of MainActivity to another of PubSearchSettingsActivity,
     * passing a given PubSearchParams object under MainActivity.CURRENT_PUB_SEARCH_PARAMS_KEY
     * (the MainActivity will wait for result PUB_SEARCH_ACTIVITY_REQUEST_CODE)
     *
     * @param mainActivity  context for the intent created during the operation
     * @return              a reference to the intent created (useful for testing)
     */
    public static Intent fromMainActivityToPubSearchActivity(final MainActivity mainActivity,
                                                             @NonNull PubSearchParams currentSearchParams,
                                                             boolean showDistanceSelector) {

        Intent i = new Intent(mainActivity, PubSearchSettingsActivity.class);
        i.putExtra(MainActivity.CURRENT_PUB_SEARCH_PARAMS_KEY, currentSearchParams);
        i.putExtra(MainActivity.SHOW_DISTANCE_SELECTOR_KEY, showDistanceSelector);
        mainActivity.startActivityForResult(i, PUB_SEARCH_ACTIVITY_REQUEST_CODE);

        return i;
    }

    /**
     * Returns from an instance of PubSearchSettingsActivity to the previous activity,
     * sending back a new PubSearchParams object.
     *
     * @param pubSearchSettingsActivity   context for the intent created during the operation
     * @param newSearchParams             the new search settings that have been set on the Activity
     * @return                            a reference to the intent created (useful for testing)
     */
    public static Intent backFromPubSearchActivity(final PubSearchSettingsActivity pubSearchSettingsActivity,
                                                   @NonNull PubSearchParams newSearchParams) {

        Intent i = new Intent();
        i.putExtra(MainActivity.NEW_PUB_SEARCH_PARAMS_KEY, newSearchParams);
        pubSearchSettingsActivity.setResult(RESULT_OK, i);
        pubSearchSettingsActivity.finish();

        return i;
    }

    public static Intent fromMainActivityToNewUserActivity(final MainActivity mainActivity) {
        Intent i = new Intent(mainActivity, NewUserActivity.class);
        mainActivity.startActivity(i);

        return i;
    }
}
