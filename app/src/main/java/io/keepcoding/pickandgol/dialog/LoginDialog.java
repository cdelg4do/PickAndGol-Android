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
                .setTitle(R.string.login_dialog_login_title)
                .setPositiveButton(R.string.login_dialog_login_button, null)   // Will be set after dialog creation
                .setNegativeButton(R.string.login_dialog_cancel_button, null)
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
                            Utils.simpleDialog(context, context.getString(R.string.login_dialog_invalid_email_title),
                                    context.getString(R.string.login_dialog_invalid_email_message));
                            txtEmail.requestFocus();
                        }

                        else if ( !Utils.isValidPassword(password) ) {
                            Utils.simpleDialog(context, context.getString(R.string.login_dialog_invalid_password_title),
                                    context.getString(R.string.login_dialog_invalid_password_message));
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
