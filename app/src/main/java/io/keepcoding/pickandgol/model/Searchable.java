package io.keepcoding.pickandgol.model;

import android.support.annotation.Nullable;


/**
 * This interface defines the search behavior for a container of Collectible objects
 */
public interface Searchable<T extends Collectible> {

    @Nullable T search(String id);
}
