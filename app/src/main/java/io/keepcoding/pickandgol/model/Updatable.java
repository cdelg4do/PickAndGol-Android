package io.keepcoding.pickandgol.model;

import java.util.List;


/**
 * This interface defines the editing behavior for a container of Collectible objects
 */
public interface Updatable<T extends Collectible> {

    void add(T element);
    void delete(T element);
    void update(T element, int index);
    void setAll(List<T> list);
}
