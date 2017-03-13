package io.keepcoding.pickandgol.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.EventListAdapter;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.view.EndlessRecyclerViewScrollListener;
import io.keepcoding.pickandgol.view.SpaceItemDecoration;

import static android.R.color.holo_blue_bright;
import static android.R.color.holo_blue_dark;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;


/**
 * This fragment represents shows an event list using a RecyclerView.
 */
public class EventListFragment extends Fragment {

    public static final String FRAGMENT_EVENTS_KEY = "FRAGMENT_EVENTS_KEY";
    public static final int COLUMNS_PORTRAIT = 2;
    public static final int COLUMNS_LANDSCAPE = 3;

    // This must be implemented by the calling Activity to respond to actions on the RecyclerView
    public interface EventListListener {

        void onItemClicked(Event event, int position);
        void onSwipeRefresh(@Nullable SwipeRefreshLayout swipe);
        void onLoadNextPage();
    }

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private EventListAdapter adapter;
    private EventAggregate events;
    private EventListListener listener;


    public static EventListFragment newInstance(EventAggregate events) {

        EventListFragment fragment = new EventListFragment();

        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_EVENTS_KEY, events);
        fragment.setArguments(args);

        return fragment;
    }

    // Required empty public constructor
    //public EventListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to recover the arguments passed from newInstance()
        if (getArguments() != null) {
            events = (EventAggregate) getArguments().getSerializable(FRAGMENT_EVENTS_KEY);
        }
    }

    // This method is called when a fragment is first attached to its context
    // (for devices having at least API 23 OR using android.support.v4.app.Fragment)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure that the fragment context implements the OnTableSelectedListener interface:
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

    @Override
    public void onDetach() {
        super.onDetach();

        // Remove the reference to the listener
        listener = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_event_list_swipe_container);
        swipeContainer.setColorSchemeResources(holo_blue_dark, holo_blue_bright);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.onSwipeRefresh(swipeContainer);
            }
        });

        setupRecyclerView(rootView);

        return rootView;
    }


    // Configures the fragment RecyclerView
    private void setupRecyclerView(View rootView) {

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_event_list_recycler);

        int columns;
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE)
            columns = COLUMNS_LANDSCAPE;
        else
            columns = COLUMNS_PORTRAIT;

        //StaggeredGridLayoutManager layoutMgr = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        GridLayoutManager layoutMgr = new GridLayoutManager(getActivity(), columns, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutMgr);

        SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(48, columns);
        recyclerView.addItemDecoration(spaceDecoration);

        adapter = new EventListAdapter(getActivity(), events);
        adapter.setOnEventClickListener(listener);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutMgr) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                listener.onLoadNextPage();
            }
        });
    }

    // Refreshes the list view
    private void syncView() {

        adapter.notifyDataSetChanged();
        //adapter = new EventListAdapter(getActivity(), events);
        //recyclerView.setAdapter(adapter);
    }


    //
    public void setEventsAndUpdateUI(EventAggregate events) {
        this.events = events;
        syncView();
    }


    public void addMoreEvents(EventAggregate moreEvents) {

        adapter.addMoreItems(moreEvents);
        adapter.notifyDataSetChanged();
    }
}
