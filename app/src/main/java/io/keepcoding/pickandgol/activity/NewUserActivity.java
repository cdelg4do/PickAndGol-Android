package io.keepcoding.pickandgol.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.interactor.RegisterNewUserInteractor;
import io.keepcoding.pickandgol.util.Utils;

public class NewUserActivity extends AppCompatActivity {
    @BindView(R.id.activity_new_user_button_cancel)
    Button btnCancel;

    @BindView(R.id.activity_new_user_button_sign_in)
    Button btnSigin;

    @BindView(R.id.activity_new_user_name_text)
    EditText txtName;

    @BindView(R.id.activity_new_user_email_text)
    EditText txtEmail;

    @BindView(R.id.activity_new_user_password_text)
    EditText txtPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        ButterKnife.bind(this);

        setupActionBar();
        setupButtons();
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFormData()) {
                    registerNewUser();
                }
            }
        });
    }

    private boolean validateFormData() {
        final String name = txtName.getText().toString();
        final String email = txtEmail.getText().toString();
        final String password = txtPassword.getText().toString();

        if (name.isEmpty()) {
            Utils.simpleDialog(this, getString(R.string.new_user_activity_validate_error_title), getString(R.string.new_user_activity_validate_error_name));
            txtName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            Utils.simpleDialog(this, getString(R.string.new_user_activity_validate_error_title), getString(R.string.new_user_activity_validate_error_email));
            txtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            Utils.simpleDialog(this, getString(R.string.new_user_activity_validate_error_title), getString(R.string.new_user_activity_validate_error_password));
            txtPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerNewUser() {
        final String name = txtName.getText().toString();
        final String email = txtEmail.getText().toString();
        final String password = txtPassword.getText().toString();

        RegisterNewUserInteractor interactor = new RegisterNewUserInteractor();
        interactor.execute(this, name, email, password, new RegisterNewUserInteractor.Listener() {
            @Override
            public void onSuccess() {
                Utils.simpleDialog(NewUserActivity.this, "Sign in", "User registered", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
            }

            @Override
            public void onFail(final String message) {
                Utils.simpleDialog(NewUserActivity.this, "Error", message);
            }
        });
    }

    private void setupActionBar() {
        setTitle(getString(R.string.activity_new_user_title));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
