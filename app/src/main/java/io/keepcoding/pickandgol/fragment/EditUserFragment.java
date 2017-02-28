package io.keepcoding.pickandgol.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.util.Utils;

/**
 *
 */
public class EditUserFragment extends Fragment {
    public interface Listener {
        void onSaveButtonPushed(final User userModified);
    }

    private static final String ARG_USER = "EditUserFragment.ARG_USER";

    @BindView(R.id.edit_user_email_edit_text)
    EditText emailText;

    @BindView(R.id.edit_user_name_edit_text)
    EditText nameText;

    @BindView(R.id.edit_user_old_password_edit_text)
    EditText oldPasswordText;

    @BindView(R.id.edit_user_new_password_edit_text)
    EditText newPasswordText;

    @BindView(R.id.edit_user_circle_image)
    ImageView userImage;

    @BindView(R.id.edit_user_save_button)
    Button saveButton;

    private User user;
    private Listener listener;

    public EditUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditUserFragment.
     */
    public static EditUserFragment newInstance(final User user) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_USER, user);
        EditUserFragment fragment = new EditUserFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_user, container, false);
        ButterKnife.bind(this, view);

        loadUserDataInWidgets();
        setupSaveButton();

        return view;
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    private void loadUserDataInWidgets() {
        if (user != null) {
            emailText.setText(user.getEmail());
            nameText.setText(user.getName());

            Picasso.with(getContext())
                    .load(user.getPhotoUrl())
                    .into(userImage);
        }
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validatePasswordEntries()) {
                    return;
                }

                if (listener != null) {
                    listener.onSaveButtonPushed(createUserWithModifiedFields());
                }
            }
        });
    }

    private boolean validatePasswordEntries() {
        boolean isOldPasswordEmpty = oldPasswordText.getText().toString().isEmpty();
        boolean isNewPasswordEmpty = newPasswordText.getText().toString().isEmpty();

        if ((isOldPasswordEmpty && !isNewPasswordEmpty) || (!isOldPasswordEmpty && isNewPasswordEmpty)) {
            Utils.simpleDialog(getActivity(), "User", "To change the password you have to write both, old and new passwords");
            return false;
        }

        return true;
    }

    private User createUserWithModifiedFields() {
        User userModified = new User(user.getId());
        final String name = nameText.getText().toString();
        final String email = emailText.getText().toString();
        final String oldPassword = oldPasswordText.getText().toString();
        final String newPassword = newPasswordText.getText().toString();

        if (user.getName() != null && name != null && !name.equals(user.getName())) {
            userModified.setName(name);
        }

        if (user.getEmail() != null && email != null && !email.equals(user.getEmail())) {
            userModified.setEmail(email);
        }

        if (!oldPassword.isEmpty()) {
            userModified.setOldPassword(oldPassword);
        }

        if (!newPassword.isEmpty()) {
            userModified.setNewPassword(newPassword);
        }

        return userModified;
    }
}
