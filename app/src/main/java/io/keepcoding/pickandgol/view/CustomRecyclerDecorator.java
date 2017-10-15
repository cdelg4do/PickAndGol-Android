package io.keepcoding.pickandgol.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;


/**
 * This class defines a decorator to adjust the offset of RecyclerView cells,
 * depending on the column they are in and how many columns are showing (2 or 3).
 */
public class CustomRecyclerDecorator extends RecyclerView.ItemDecoration {

    private int offset2col;
    private int offset3col;


    public CustomRecyclerDecorator(int offset2col, int offset3col) {
        this.offset2col = offset2col;
        this.offset3col = offset3col;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        RecyclerView.LayoutManager layoutMgr = parent.getLayoutManager();

        // These settings will apply only in case the RecyclerView uses a staggered grid layout
        if ( layoutMgr instanceof StaggeredGridLayoutManager) {

            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)view .getLayoutParams();

            int layoutTotalColumns = ((StaggeredGridLayoutManager )layoutMgr).getSpanCount();
            //int itemAdapterPos = parent.getChildAdapterPosition(view);
            int itemCol = lp.getSpanIndex();

            if (layoutTotalColumns == 2) {

                if (itemCol == 0)   outRect.right = -offset2col; // -24
                else                outRect.left = -offset2col;
            }

            else if (layoutTotalColumns == 3) {

                if (itemCol == 0)       outRect.right = -offset3col;    // -32
                else if (itemCol == 2)  outRect.left = -offset3col;
                else {
                    outRect.right = -offset3col / 2;    // -16
                    outRect.left = -offset3col / 2;
                }
            }
        }
    }
}
