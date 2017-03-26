package io.keepcoding.pickandgol.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.PubListAdapter;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.view.EndlessRecyclerViewScrollListener;
import io.keepcoding.pickandgol.view.SpaceItemDecoration;

import static android.R.color.holo_blue_bright;
import static android.R.color.holo_blue_dark;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;


/**
 * This fragment represents shows a pub list using a RecyclerView.
 */
public class PubListFragment extends Fragment {

    public static final String FRAGMENT_PUBS_KEY = "FRAGMENT_PUBS_KEY";
    public static final int COLUMNS_PORTRAIT = 2;
    public static final int COLUMNS_LANDSCAPE = 3;

    // This must be implemented by the calling Activity to respond to actions on the RecyclerView
    public interface PubListListener {

        void onPubClicked(Pub pub, int position);
        void onPubListSwipeRefresh(@Nullable SwipeRefreshLayout swipe);
        void onPubListLoadNextPage();
    }

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private PubListAdapter adapter;
    private PubAggregate pubs;
    private PubListListener listener;


    public static PubListFragment newInstance(PubAggregate pubs) {

        PubListFragment fragment = new PubListFragment();

        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_PUBS_KEY, pubs);
        fragment.setArguments(args);

        return fragment;
    }

    // Required empty public constructor
    //public PubListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to recover the arguments passed from newInstance()
        if (getArguments() != null) {
            pubs = (PubAggregate) getArguments().getSerializable(FRAGMENT_PUBS_KEY);
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

    @Override
    public void onDetach() {
        super.onDetach();

        // Remove the reference to the listener
        listener = null;
    }


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

        int columns;
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE)
            columns = COLUMNS_LANDSCAPE;
        else
            columns = COLUMNS_PORTRAIT;

        StaggeredGridLayoutManager layoutMgr = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        //GridLayoutManager layoutMgr = new GridLayoutManager(getActivity(), columns, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutMgr);

        SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(48, columns);
        recyclerView.addItemDecoration(spaceDecoration);

        adapter = new PubListAdapter(getActivity(), pubs);
        adapter.setOnPubClickListener(listener);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutMgr) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                listener.onPubListLoadNextPage();
            }
        });
    }

    // Refreshes the list view
    private void syncView() {
        adapter.notifyDataSetChanged();
    }

    // Adds more pubs to the adapter data source, then refresh the list
    public void addMorePubs(PubAggregate morePubs) {

        adapter.addMoreItems(morePubs);
        adapter.notifyDataSetChanged();
    }
}
