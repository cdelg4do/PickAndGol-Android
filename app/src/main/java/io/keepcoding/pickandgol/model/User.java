package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.List;


/**
 * This class represents a User in the system.
 * It implements the Serializable interface so that it can be passed inside an Intent.
 */
public class User implements Serializable {

    private @NonNull String id;
    private @NonNull String email;
    private @NonNull String name;
    private @NonNull List<Integer> favorites;
    private @Nullable String photoUrl;
    private @Nullable String oldPassword;
    private @Nullable String newPassword;

    private User() {
    }

    public User(final String id) {
        this.id = id;
    }

    public User(final @NonNull String id,
                final @NonNull String email,
                final @NonNull String name,
                final @NonNull List<Integer> favorites,
                final @Nullable String photoUrl) {

        this.id = id;
        this.email = email;
        this.name = name;
        this.favorites = favorites;
        this.photoUrl = photoUrl;
    }


    // Getters:

    @NonNull
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @NonNull
    public User setEmail(final String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    @NonNull
    public User setName(final String name) {
        this.name = name;
        return this;
    }

    @NonNull
    public List<Integer> getFavorites() {
        return favorites;
    }

    @Nullable
    public String getPhotoUrl() {
        return photoUrl;
    }

    @NonNull
    public User setPhotoUrl(final String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    @Nullable
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(@Nullable String oldPassword) {
        this.oldPassword = oldPassword;
    }

    @Nullable
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(@Nullable String newPassword) {
        this.newPassword = newPassword;
    }
}
