package io.keepcoding.pickandgol.util;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;


/**
 * This class provides useful common auxiliary functions.
 * This class is abstract, and all its methods are static.
 */
public class Utils {

    public static void shortSnack(Activity activity, String msg) {
        showSnackMessage(activity, msg, Snackbar.LENGTH_SHORT);
    }

    public static void longSnack(Activity activity, String msg) {
        showSnackMessage(activity, msg, Snackbar.LENGTH_LONG);
    }

    public static void permanentSnack(Activity activity, String msg) {
        showSnackMessage(activity, msg, Snackbar.LENGTH_INDEFINITE);
    }

    private static void showSnackMessage(Activity activity, String msg, int duration) {
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, msg, duration).show();
    }


    public static void shortToast(Context context, String msg) {
        showToastMessage(context, msg, Toast.LENGTH_SHORT);
    }

    public static void longToast(Context context, String msg) {
        showToastMessage(context, msg, Toast.LENGTH_LONG);
    }

    private static void showToastMessage(Context context, String msg, int duration) {
        Toast.makeText(context, msg, duration).show();
    }
}
