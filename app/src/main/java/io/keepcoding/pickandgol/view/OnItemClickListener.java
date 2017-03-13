package io.keepcoding.pickandgol.view;

/**
 * This generic interface must be implemented by any object
 * that listens for a ListView/GridView/RecyclerView item to be clicked.
 *
 * @param <T> the type of the object represented by the element clicked.
 */
public interface OnItemClickListener<T> {

    void onItemClicked(T item, int position);
}
