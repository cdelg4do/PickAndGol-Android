package io.keepcoding.pickandgol.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.manager.image.ImageManager;


/**
 * This fragment represents the content of the main activity
 */
public class MainContentFragment extends Fragment {

    public static final String FRAGMENT_IMAGE_URI_KEY = "FRAGMENT_IMAGE_URI_KEY";
    public static final String FRAGMENT_CAPTION_KEY = "FRAGMENT_CAPTION_KEY";

    private ImageView fragmentImage;

    /**
     * Creates a new instance of {@link MainContentFragment}
     *
     * @imageUrl    url of the image to show in the fragment, if any
     * @param title the string to show in the fragment
     * @return      a new instance of the fragment
     */
    public static MainContentFragment newInstance(String imageUrl, String title) {

        MainContentFragment fragment = new MainContentFragment();

        Bundle args = new Bundle();

        if (imageUrl != null)
            args.putString(FRAGMENT_IMAGE_URI_KEY, imageUrl);

        if (title != null)
            args.putString(FRAGMENT_CAPTION_KEY, title);

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
        String imageUrl = getArguments().getString(FRAGMENT_IMAGE_URI_KEY, null);
        String caption = getArguments().getString(FRAGMENT_CAPTION_KEY, null);

        // Update the fragment content
        fragmentImage = (ImageView) view.findViewById(R.id.fragment_image);

        if (imageUrl != null) {

            fragmentImage.setVisibility(View.VISIBLE);

            ImageManager.getInstance(getActivity()).loadImage(
                    imageUrl,
                    fragmentImage,
                    R.drawable.error_placeholder);
        }
        else
            fragmentImage.setVisibility(View.GONE);

        TextView fragmentText = (TextView) view.findViewById(R.id.fragment_text);
        if (caption != null && fragmentText != null)
            fragmentText.setText(caption);

        return view;
    }

}
