package io.keepcoding.pickandgol.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.util.Utils;


/**
 * This class represents a login dialog
 */
public class LoginDialog {

    private EditText txtEmail, txtPwd;

    private AlertDialog dialog;
    private LoginDialogListener listener;

    public interface LoginDialogListener {
        void onLoginClick(String email, String password);
    }

    public LoginDialog(final Activity context, final LoginDialogListener listener) {
        this.listener = listener;

        LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = inflater.inflate(R.layout.dialog_login, null);

        this.txtEmail = (EditText) contentView.findViewById(R.id.dialog_login_text_email);
        this.txtPwd = (EditText) contentView.findViewById(R.id.dialog_login_text_password);

        this.dialog = new AlertDialog.Builder(context)
                .setView(contentView)
                .setCancelable(false)
                .setTitle("Login to Pick And Gol")
                .setPositiveButton("Login", null)   // Will be set after dialog creation
                .setNegativeButton("Cancel", null)
                .create();

        // Do this to prevent the dialog from closing immediately after clicking login/cancel button
        // (if input validation fails, we want to keep showing the dialog)
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        String email = txtEmail.getText().toString();
                        String password = txtPwd.getText().toString();

                        if ( !Utils.isValidEmail(email) ) {
                            Utils.simpleDialog(context, "Invalid email", "Please insert a valid email address");
                            txtEmail.requestFocus();
                        }

                        else if ( !Utils.isValidPassword(password) ) {
                            Utils.simpleDialog(context, "Invalid password", "Password length must be between 6-30 characters");
                            txtPwd.requestFocus();
                        }

                        else {
                            listener.onLoginClick(email, Utils.encryptPassword(password));
                            dialog.dismiss();
                        }
                    }
                });
            }
        });


    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
