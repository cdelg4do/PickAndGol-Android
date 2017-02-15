package io.keepcoding.pickandgol.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.keepcoding.pickandgol.R;


/**
 * This fragment represents the content of the main activity
 */
public class MainContentFragment extends Fragment {

    public static final String FRAGMENT_TITLE_KEY = "FRAGMENT_TITLE_KEY";


    /**
     * Creates a new instance of {@link MainContentFragment}
     *
     * @param title the string to show in the fragment
     * @return a new instance of the fragment
     */
    public static MainContentFragment newInstance(String title) {

        MainContentFragment fragment = new MainContentFragment();

        Bundle args = new Bundle();
        args.putString(FRAGMENT_TITLE_KEY, title);
        fragment.setArguments(args);

        return fragment;
    }


    // Required empty default constructor
    public MainContentFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_content, container, false);

        // Recover the arguments passed from newInstance()
        String title = getArguments().getString(FRAGMENT_TITLE_KEY);

        // Update the fragment content
        TextView fragmentText = (TextView) view.findViewById(R.id.fragment_text);
        fragmentText.setText(title);

        return view;
    }

}
