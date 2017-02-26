package io.keepcoding.pickandgol.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.util.Utils;


/**
 * This class represent a filepath choose dialog
 */
public class ChooseLocalFileDialog {

    private EditText txtFolder, txtFile;

    private AlertDialog dialog;
    private ChooseFileDialogListener listener;

    public interface ChooseFileDialogListener {
        void onChooseFileClick(String filePath);
    }

    public ChooseLocalFileDialog(final Activity context, String defaultFolder, String defaultFile, final ChooseFileDialogListener listener) {
        this.listener = listener;

        LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = inflater.inflate(R.layout.dialog_choose_local_file, null);

        this.txtFolder = (EditText) contentView.findViewById(R.id.dialog_choose_file_text_folder);
        this.txtFile = (EditText) contentView.findViewById(R.id.dialog_choose_file_text_file);

        String dFolder = (defaultFolder != null) ? defaultFolder : "";
        String dFile = (defaultFile != null) ? defaultFile : "";

        txtFolder.setText(dFolder);
        txtFile.setText(dFile);

        this.dialog = new AlertDialog.Builder(context)
                .setView(contentView)
                .setCancelable(false)
                .setTitle("Enter a file to upload")
                .setPositiveButton("Choose File", null)   // Will be set after dialog creation
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

                        String folder = txtFolder.getText().toString();
                        String file = txtFile.getText().toString();

                        String filePath = folder +"/"+ file;

                        if ( !new File(folder).isDirectory() ) {
                            Utils.simpleDialog(context, "Folder not found", "Please enter an existing folder path.");
                            txtFolder.requestFocus();
                        }

                        else if ( !new File(filePath).exists() ) {
                            Utils.simpleDialog(context, "File not found", "Please enter a file from the folder.");
                            txtFile.requestFocus();
                        }

                        else {
                            listener.onChooseFileClick(filePath);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    public ChooseLocalFileDialog(final Activity context, final ChooseFileDialogListener listener) {
        new ChooseLocalFileDialog(context, null, null, listener);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
