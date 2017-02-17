package io.keepcoding.pickandgol.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;


/**
 * This class contains the login info of the device session.
 * It implements the Serializable interface so that it can be passed inside an Intent.
 */
public class Login implements Serializable {

    private boolean anonymousSession;
    private @Nullable String id;
    private @Nullable String email;
    private @Nullable String name;
    private @Nullable String token;
    private @Nullable String photoUrl;

    // The default constructor will not be public
    private Login() {
    }

    public Login(final @Nullable String id,
                 final @Nullable String email,
                 final @Nullable String name,
                 final @Nullable String token,
                 final @Nullable String photoUrl) {

        this.id = id;
        this.email = email;
        this.name = name;
        this.token = token;
        this.photoUrl = photoUrl;

        anonymousSession = (id != null && email != null && name != null && token != null);
    }


    // Getters:

    public boolean isAnonymousSession() {
        return anonymousSession;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    @Nullable
    public String getPhotoUrl() {
        return photoUrl;
    }
}
