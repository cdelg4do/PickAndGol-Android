package io.keepcoding.pickandgol.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import java.util.regex.Pattern;


/**
 * This class provides useful common auxiliary functions.
 * This class is abstract, and all its methods are static.
 */
public class Utils {

    /*
        Screen messages and dialogs
     */

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


    // Returns a new indeterminate, non-cancelable progress dialog with a given message
    public static ProgressDialog newProgressDialog(Context ctx, String msg) {

        final ProgressDialog pDialog = new ProgressDialog(ctx);
        pDialog.setTitle("Please wait");
        pDialog.setMessage(msg);
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);

        return pDialog;
    }


    // Shows the user a dialog with Accept button only and its corresponding listener
    public static void simpleDialog(Context ctx, String title, String msg, DialogInterface.OnClickListener acceptListener) {

        final AlertDialog dialog;

        // If no listener was provided, use a default one
        if (acceptListener == null)
            acceptListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            };

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(ctx.getResources().getString(android.R.string.ok), acceptListener);

        dialog = builder.create();
        dialog.show();
    }

    // Shows the user a dialog with Accept button (with the default listener)
    public static void simpleDialog(Context ctx, String title, String msg) {
        simpleDialog(ctx, title, msg, null);
    }


    /*
        String operations and validators
     */

    public static boolean isValidEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence cs = email;

        Pattern patron = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        if ( patron.matcher(cs).matches() )
            isValid = true;

        return isValid;
    }

    public static boolean isValidPassword(String password) {

        return (password.length() >= 6) && (password.length() <= 30);
    }

    public static String encryptPassword(String password) {
        //TODO: implement password encryption
        return password;
    }

    public static String safeString(String string) {

        if (string == null)
            return "'null'";

        return string;
    }

}
