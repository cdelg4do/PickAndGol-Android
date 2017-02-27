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
 * This class represent an url choose dialog
 */
public class ChooseRemoteUrlDialog {

    private EditText txtRemoteUrl;

    private AlertDialog dialog;
    private ChooseRemoteUrlListener listener;

    public interface ChooseRemoteUrlListener {
        void onChooseRemoteUrl(String url);
    }

    public ChooseRemoteUrlDialog(final Activity context, String title, String defaultUrl, final ChooseRemoteUrlListener listener) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = inflater.inflate(R.layout.dialog_choose_remote_url, null);

        this.txtRemoteUrl = (EditText) contentView.findViewById(R.id.dialog_choose_remote_url_text);

        String dUrl = (defaultUrl != null) ? defaultUrl : "";
        txtRemoteUrl.setText(dUrl);

        this.dialog = new AlertDialog.Builder(context)
                .setView(contentView)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("Accept", null)   // Will be set after dialog creation
                .setNegativeButton("Cancel", null)
                .create();

        // Do this to prevent the dialog from closing immediately after clicking upload/cancel button
        // (if input validation fails, we want to keep showing the dialog)
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        String url = txtRemoteUrl.getText().toString();

                        if ( ! Utils.isValidUrl(url) ) {
                            Utils.simpleDialog(context, "Invalid url", "Please enter a valid url.");
                            txtRemoteUrl.requestFocus();
                        }

                        else {
                            listener.onChooseRemoteUrl(url);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    public ChooseRemoteUrlDialog(final Activity context, String title, final ChooseRemoteUrlListener listener) {
        new ChooseRemoteUrlDialog(context, title, null, listener);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
