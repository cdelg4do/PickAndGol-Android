package io.keepcoding.pickandgol.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;


/**
 * This interface defines the iteration behavior for a container of Collectible objects
 */
public interface Iterable<T extends Collectible> {

    int size();
    @Nullable T get(int index);
    @NonNull List<T> getAll();
}
