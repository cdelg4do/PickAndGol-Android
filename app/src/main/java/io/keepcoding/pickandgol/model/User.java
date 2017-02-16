package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;

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


    // The default constructor will not be public
    private User() {
    }

    public User(final @NonNull String id,
                final @NonNull String email,
                final @NonNull String name,
                final @NonNull List<Integer> favorites) {

        this.id = id;
        this.email = email;
        this.name = name;
        this.favorites = favorites;
    }


    // Getters:

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
    public List<Integer> getFavorites() {
        return favorites;
    }
}
