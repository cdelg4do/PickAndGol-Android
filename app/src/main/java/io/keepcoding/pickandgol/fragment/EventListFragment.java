package io.keepcoding.pickandgol.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.EventListAdapter;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.view.EndlessRecyclerViewScrollListener;
import io.keepcoding.pickandgol.view.EventListListener;
import io.keepcoding.pickandgol.view.CustomRecyclerDecorator;
import io.keepcoding.pickandgol.view.SpaceItemDecoration;

import static android.R.color.holo_blue_bright;
import static android.R.color.holo_blue_dark;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static io.keepcoding.pickandgol.adapter.EventListAdapter.LayoutType.CELLS;
import static io.keepcoding.pickandgol.adapter.EventListAdapter.LayoutType.ROWS;


/**
 * This fragment shows an event list using a RecyclerView.
 * It can represent the data in a "classic" list, or in the form of a cell grid.
 */
public class EventListFragment extends Fragment {

    // Number of rows to show if using a grid layout (for portrait and landscape)
    private static final int GRID_COLUMNS_PORTRAIT = 2;
    private static final int GRID_COLUMNS_LANDSCAPE = 3;

    // Key strings for arguments to initialize the fragment
    private static final String FRAGMENT_INITIAL_EVENTS_KEY = "FRAGMENT_INITIAL_EVENTS_KEY";
    private static final String USE_ROW_LAYOUT_KEY = "USE_ROW_LAYOUT_KEY";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private EventListAdapter adapter;
    private EventListListener listener;
    private EventAggregate events;
    private boolean useRowLayout;

    /**
     * Call this to initialize a new fragment instance.
     *
     * @param initialEvents the events that the fragment will show in the beginning
     * @param useRowLayout  true: a "classic" list will be used; false: use a grid layout
     * @return              a reference to the new fragment object
     */
    public static EventListFragment newInstance(EventAggregate initialEvents,
                                                boolean useRowLayout) {

        EventListFragment fragment = new EventListFragment();

        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_INITIAL_EVENTS_KEY, initialEvents);
        args.putBoolean(USE_ROW_LAYOUT_KEY, useRowLayout);
        fragment.setArguments(args);

        return fragment;
    }

    // When the fragment is created, recover the initial arguments
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            events = (EventAggregate) getArguments().getSerializable(FRAGMENT_INITIAL_EVENTS_KEY);
            useRowLayout = getArguments().getBoolean(USE_ROW_LAYOUT_KEY, false);
        }
    }

    // This method is called when a fragment is first attached to its context
    // (for devices having at least API 23 OR using android.support.v4.app.Fragment)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure that the fragment context implements the EventListListener interface:
        // If so, keep the reference to it.
        // If not, throw an exception (will terminate the program).
        if (context instanceof EventListListener)
            listener = (EventListListener) context;
        else
            throw new RuntimeException(context.toString() +" must implement EventListListener");
    }

    // Same as previous, but using the deprecated onAttach(Activity) instead the newer onAttach(Context)
    // (called when using android.app.Fragment, or when device API is under 23)
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getActivity() instanceof EventListListener)
            listener = (EventListListener) getActivity();
        else
            throw new RuntimeException(getActivity().toString() +" must implement EventListListener");
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

        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_event_list_swipe_container);
        swipeContainer.setColorSchemeResources(holo_blue_dark, holo_blue_bright);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.onEventListSwipeRefresh(swipeContainer);
            }
        });

        setupRecyclerView(rootView);

        return rootView;
    }

    // Configures the RecyclerView
    private void setupRecyclerView(View rootView) {

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_event_list_recycler);

        // Decorator to tune the exact position of each cell
        recyclerView.addItemDecoration( new CustomRecyclerDecorator(24, 32) );

        // If the list is represented in the form of a classic row list
        if (useRowLayout) {

            LinearLayoutManager layoutMgr = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutMgr);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutMgr.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);

            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutMgr) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    listener.onEventListLoadNextPage();
                }
            });

            adapter = new EventListAdapter(getActivity(), events, ROWS);
            adapter.setOnEventClickListener(listener);
            recyclerView.setAdapter(adapter);
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
                    listener.onEventListLoadNextPage();
                }
            });

            adapter = new EventListAdapter(getActivity(), events, CELLS);
            adapter.setOnEventClickListener(listener);
            recyclerView.setAdapter(adapter);
        }
    }

    // Adds more events to the adapter data source, then refresh the list
    public void addMoreEvents(EventAggregate moreEvents) {

        adapter.addMoreItems(moreEvents);
        adapter.notifyDataSetChanged();
    }
}
