package io.keepcoding.pickandgol.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.util.Utils;


/**
 * This class represent a filepath choose dialog
 */
public class ChooseRemoteFileDialog {

    private EditText txtRemoteFileName;

    private AlertDialog dialog;
    private ChooseRemoteFileListener listener;

    public interface ChooseRemoteFileListener {
        void onChooseRemoteFile(String url);
    }

    public ChooseRemoteFileDialog(final Activity context, String title, String defaultFileName, final ChooseRemoteFileListener listener) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = inflater.inflate(R.layout.dialog_choose_remote_file, null);

        this.txtRemoteFileName = (EditText) contentView.findViewById(R.id.dialog_choose_remote_file_text);

        String dFileName = (defaultFileName != null) ? defaultFileName : "";
        txtRemoteFileName.setText(dFileName);

        this.dialog = new AlertDialog.Builder(context)
                .setView(contentView)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("Choose File Name", null)   // Will be set after dialog creation
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

                        String fileName = txtRemoteFileName.getText().toString();

                        if ( fileName.equals("") ) {
                            Utils.simpleDialog(context, "Empty file name", "Please enter a valid file name.");
                            txtRemoteFileName.requestFocus();
                        }

                        else {
                            String url = ImageManager.getImageUrl(fileName);
                            listener.onChooseRemoteFile(url);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    public ChooseRemoteFileDialog(final Activity context, String title, final ChooseRemoteFileListener listener) {
        new ChooseRemoteFileDialog(context, title, null, listener);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
