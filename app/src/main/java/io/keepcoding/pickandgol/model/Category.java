package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;

import java.io.Serializable;


/**
 * This class contains the info about an event category in the system.
 * (implements the Serializable interface so that it can be passed inside an Intent)
 */
public class Category implements Collectible, Serializable {

    private String id;
    private String name;

    public Category(@NonNull final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
