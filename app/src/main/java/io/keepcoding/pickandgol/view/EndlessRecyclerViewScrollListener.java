package io.keepcoding.pickandgol.view;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * This class is an implementation of RecyclerView.OnScrollListener to trigger the load of the
 * next page of results for a RecyclerView when we scroll to some point, before the end of the list.
 *
 * This is an abstract class, so it must be inherited by another class that implements onLoadMore().
 */
public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    // Initialize this with the minimum number of "rows" to have below the current scroll position,
    // before loading more items into the RecyclerView.
    private int visibleThreshold = 5;

    private int currentPage = 0;            // Stores the current offset index of data you have loaded
    private int previousTotalItemCount = 0; // Stores the total no. of items in the data set after the last load
    private boolean loading = true;         // True if we are still waiting for the last set of data to load.
    private int startingPageIndex = 0;      // Sets the starting page index


    // Constructors for the different LayoutManagers that a RecyclerView can have:
    RecyclerView.LayoutManager mLayoutManager;

    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }


    // This method is called many times per second during a scroll.
    // It will call onLoadMore() in case the RecyclerView scrolled down beyond the visibleThreshold.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {

        int lastVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        }

        else if (mLayoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        }

        else if (mLayoutManager instanceof StaggeredGridLayoutManager) {

            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(null);

            for (int i = 0; i < lastVisibleItemPositions.length; i++) {

                if (i == 0)
                    lastVisibleItemPosition = lastVisibleItemPositions[i];

                else if (lastVisibleItemPositions[i] > lastVisibleItemPosition)
                    lastVisibleItemPosition = lastVisibleItemPositions[i];
            }
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {

            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;

            if (totalItemCount == 0)
                this.loading = true;
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {

            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {

            //Log.d("EndlessScroll","lastVisibleItemPosition: "+ lastVisibleItemPosition
            //        +"\nvisibleThreshold: "+ visibleThreshold +"\ntotalItemCount: "+ totalItemCount);

            currentPage++;
            onLoadMore(currentPage, totalItemCount, view);
            loading = true;
        }
    }

    // Call this method if need to reset the listener state (i.e. to perform new searches)
    public void resetState() {

        this.currentPage = this.startingPageIndex;
        this.previousTotalItemCount = 0;
        this.loading = true;
    }

    // This method defines the process for actually loading more data based on page.
    // (it must be overridden by all the subclasses)
    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);

}
