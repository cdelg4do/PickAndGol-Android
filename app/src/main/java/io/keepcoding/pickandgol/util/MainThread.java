package io.keepcoding.pickandgol.util;

import android.os.Handler;
import android.os.Looper;


/**
 * This abstract class simplifies the task of executing a Runnable on the UI Thread.
 */
public abstract class MainThread {

    /**
     * Executes a Runnable object in the UI Thread.
     *
     * @param runnable  the Runnable object to execute in foreground.
     */
    public static void run(final Runnable runnable) {

        new Handler( Looper.getMainLooper() ).post(new Runnable() {

            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
