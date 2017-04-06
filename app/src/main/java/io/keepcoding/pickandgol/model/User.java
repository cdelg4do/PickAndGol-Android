package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.List;


/**
 * This class represents a User in the system.
 * It implements the Serializable interface so that it can be passed inside an Intent.
 */
public class User implements Serializable, Collectible {

    private @NonNull String id;
    private @NonNull String email;
    private @NonNull String name;
    private @NonNull List<String> favorites;
    private @Nullable String photoUrl;
    private @Nullable String oldPassword;
    private @Nullable String newPassword;
    private @Nullable String registrationToken;

    private User() {
    }

    public User(final String id) {
        this.id = id;
    }

    public User(final @NonNull String id,
                final @NonNull String email,
                final @NonNull String name,
                final @NonNull List<String> favorites,
                final @Nullable String photoUrl) {

        this.id = id;
        this.email = email;
        this.name = name;
        this.favorites = favorites;
        this.photoUrl = photoUrl;
    }


    // Getters:

    @Override
    public @NonNull String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public @NonNull List<String> getFavorites() {
        return favorites;
    }

    public @Nullable String getPhotoUrl() {
        return photoUrl;
    }

    public @NonNull String getOldPassword() {
        return oldPassword;
    }

    public @NonNull String getNewPassword() {
        return newPassword;
    }

    public @NonNull String getRegistrationToken() {
        return registrationToken;
    }


    // Setters:

    public @NonNull User setEmail(final String email) {
        this.email = email;
        return this;
    }

    public @NonNull User setName(final String name) {
        this.name = name;
        return this;
    }

    public User setFavorites(final List<String> favorites) {
        this.favorites = favorites;
        return this;
    }

    public @NonNull User setPhotoUrl(final String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public void setOldPassword(@Nullable String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(@Nullable String newPassword) {
        this.newPassword = newPassword;
    }

    public void setRegistrationToken(@Nullable String registrationToken) {
        this.registrationToken = registrationToken;
    }


    // Comparator method

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        User user = (User) obj;

        return (((id == null && user.id == null) || (id != null && user.id != null && id.equals(user.getId())))
                && ((name == null && user.getName() == null) || (name != null && user.getName() != null && name.equals(user.getName())))
                && ((email == null && user.getEmail() == null) || (email != null && user.getEmail() != null &&  email.equals(user.getEmail())))
                && ((photoUrl == null && user.getPhotoUrl() == null) || (photoUrl != null && user.getPhotoUrl() != null && photoUrl.equals(user.getPhotoUrl())))
                && ((favorites == null && user.getFavorites() == null) || (favorites != null && user.getFavorites() != null && favorites.equals(user.getFavorites()))));
    }
}
