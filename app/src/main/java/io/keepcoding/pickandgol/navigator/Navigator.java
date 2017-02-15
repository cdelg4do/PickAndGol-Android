package io.keepcoding.pickandgol.navigator;

import android.content.Intent;

import io.keepcoding.pickandgol.activity.MainActivity;
import io.keepcoding.pickandgol.activity.SplashActivity;

/**
 * This class manages all the navigation between the activities of the application.
 * (this is an abstract class, all its methods are public and static)
 */
public class Navigator {

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
}
