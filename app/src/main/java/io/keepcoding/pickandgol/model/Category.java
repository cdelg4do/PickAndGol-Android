package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Category implements Collectible, Serializable {
    private @NonNull String id;
    private String name;

    public Category(@NonNull final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public @NonNull String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
