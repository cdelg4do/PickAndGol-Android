package io.keepcoding.pickandgol.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * This class helps to add a layout offset to the item views from a Recycler view
 * (this is necessary since setting a margin on the items layout will not work to space them)
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;
    private final int elementsPerRow;

    /**
     * Initializes the object with the desired space measure.
     *
     * @param space         the space amount you want to separate the recycler view items.
     * @param itemsPerRow   the number of items in each row of the recycler view.
     */
    public SpaceItemDecoration(int space, int itemsPerRow) {
        this.space = space;
        this.elementsPerRow = itemsPerRow;
    }

    // Sets the spaces to apply around the item: left, right, bottom and top.
    // (top space will be set only for elements in the first row)
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        int viewPosition = parent.getChildAdapterPosition(view);
        if (viewPosition < elementsPerRow)
            outRect.top = space;
    }
}