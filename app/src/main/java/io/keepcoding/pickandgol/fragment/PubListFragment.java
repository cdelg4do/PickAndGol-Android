package io.keepcoding.pickandgol.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.PubListAdapter;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.view.EndlessRecyclerViewScrollListener;
import io.keepcoding.pickandgol.view.PubListListener;
import io.keepcoding.pickandgol.view.SpaceItemDecoration;

import static android.R.color.holo_blue_bright;
import static android.R.color.holo_blue_dark;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static io.keepcoding.pickandgol.adapter.PubListAdapter.LayoutType.CELLS;
import static io.keepcoding.pickandgol.adapter.PubListAdapter.LayoutType.ROWS;


/**
 * This fragment represents shows a pub list using a RecyclerView.
 * It can represent the data in a "classic" list, or in the form of a cell grid.
 */
public class PubListFragment extends Fragment {

    // Number of rows to show if using a grid layout (for portrait and landscape)
    public static final int GRID_COLUMNS_PORTRAIT = 2;
    public static final int GRID_COLUMNS_LANDSCAPE = 3;

    // Key strings for arguments to initialize the fragment
    public static final String FRAGMENT_INITIAL_PUBS_KEY = "FRAGMENT_INITIAL_PUBS_KEY";
    public static final String FRAGMENT_INITIAL_POSITION_KEY = "FRAGMENT_INITIAL_POSITION_KEY";
    public static final String USE_ROW_LAYOUT_KEY = "USE_ROW_LAYOUT_KEY";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private PubListAdapter adapter;
    private PubListListener listener;
    private PubAggregate pubs;
    private int initialScrollPosition;
    private boolean useRowLayout;

    /**
     * Call this to initialize a new fragment instance.
     *
     * @param initialPubs           the pubs that the fragment will show in the beginning
     * @param initialScrollPosition the scroll position the list will be located at
     * @param useRowLayout          true: a "classic" list will be used; false: use a grid layout
     * @return                      a reference to the new fragment object
     */
    public static PubListFragment newInstance(PubAggregate initialPubs,
                                              int initialScrollPosition,
                                              boolean useRowLayout) {

        PubListFragment fragment = new PubListFragment();

        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_INITIAL_PUBS_KEY, initialPubs);
        args.putInt(FRAGMENT_INITIAL_POSITION_KEY, initialScrollPosition);
        args.putBoolean(USE_ROW_LAYOUT_KEY, useRowLayout);
        fragment.setArguments(args);

        return fragment;
    }

    // When the fragment is created, recover the initial arguments
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            pubs = (PubAggregate) getArguments().getSerializable(FRAGMENT_INITIAL_PUBS_KEY);
            initialScrollPosition = getArguments().getInt(FRAGMENT_INITIAL_POSITION_KEY);
            useRowLayout = getArguments().getBoolean(USE_ROW_LAYOUT_KEY, false);
        }
    }

    // This method is called when a fragment is first attached to its context
    // (for devices having at least API 23 or when using android.support.v4.app.Fragment)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure that the fragment context implements the PubListListener interface:
        // If so, keep the reference to it.
        // If not, throw an exception (will terminate the program).
        if (context instanceof PubListListener)
            listener = (PubListListener) context;
        else
            throw new RuntimeException(context.toString() +" must implement PubListListener");
    }

    // Same as previous, but using the deprecated onAttach(Activity) instead the newer onAttach(Context)
    // (called when using android.app.Fragment, or when device API is under 23)
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getActivity() instanceof PubListListener)
            listener = (PubListListener) getActivity();
        else
            throw new RuntimeException(getActivity().toString() +" must implement PubListListener");
    }

    // When the fragment is no longer associated to the activity, remove references to the listener
    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    // When the fragment's View hierarchy is created, configure the swipe control and the recycler
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_pub_list, container, false);

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_pub_list_swipe_container);
        swipeContainer.setColorSchemeResources(holo_blue_dark, holo_blue_bright);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.onPubListSwipeRefresh(swipeContainer);
            }
        });

        setupRecyclerView(rootView);

        return rootView;
    }

    // Configures the fragment RecyclerView
    private void setupRecyclerView(View rootView) {

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_pub_list_recycler);

        // If the list is represented in the form of a classic row list
        if (useRowLayout) {

            LinearLayoutManager layoutMgr = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutMgr);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutMgr.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);

            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutMgr) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    listener.onPubListLoadNextPage();
                }
            });

            adapter = new PubListAdapter(getActivity(), pubs, ROWS);
            adapter.setOnPubClickListener(listener);
            recyclerView.setAdapter(adapter);

            layoutMgr.scrollToPosition(initialScrollPosition);
        }

        // If the list is represented in the form of a cell grid
        else {

            int columns;
            if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE)
                columns = GRID_COLUMNS_LANDSCAPE;
            else
                columns = GRID_COLUMNS_PORTRAIT;

            StaggeredGridLayoutManager layoutMgr = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
            //GridLayoutManager layoutMgr = new GridLayoutManager(getActivity(), columns, GridLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutMgr);

            SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(48, columns);
            recyclerView.addItemDecoration(spaceDecoration);

            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutMgr) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    listener.onPubListLoadNextPage();
                }
            });

            adapter = new PubListAdapter(getActivity(), pubs, CELLS);
            adapter.setOnPubClickListener(listener);
            recyclerView.setAdapter(adapter);
        }
    }

    // Gets the pubs shown so far
    public PubAggregate getPubs() {
        return pubs;
    }

    // Gets the current scroll position of the RecyclerView, useful to save the recycler state
    // (Note: does not work in case of staggered grid layout, returns null)
    public @Nullable Integer getLinearRecyclerScrollPosition() {

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof GridLayoutManager)
            return ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();

        if (layoutManager instanceof LinearLayoutManager)
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

        else
            return null;
    }

    // Adds more pubs to the adapter data source, then refresh the list
    public void addMorePubs(PubAggregate morePubs) {

        pubs.addElements(morePubs);

        adapter.addMoreItems(morePubs);
        adapter.notifyDataSetChanged();
    }
}
