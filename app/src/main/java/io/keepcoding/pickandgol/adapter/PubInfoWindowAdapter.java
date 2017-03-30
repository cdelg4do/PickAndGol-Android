package io.keepcoding.pickandgol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.model.Pub;


/**
 * This class is the adapter to generate customized Info Windows for the map's pub markers.
 */
public class PubInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final Context context;
    private final View infoWindowContents;


    public PubInfoWindowAdapter(Context context){

        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        infoWindowContents = inflater.inflate(R.layout.info_window_pub, null);
    }


    // Returns a View that will be used as a default for the entire Info Window.
    // If this method returns null, then getInfoContents() will be called.
    @Override
    public View getInfoWindow(final Marker marker) {
        return null;
    }


    // Returns a view with the customized contents of the Info Window
    // (but still keeping the default info window frame and background, if any).
    // If this method also returns null, then the default info window will be used.
    @Override
    public View getInfoContents(final Marker marker) {

        Pub pub = (Pub) marker.getTag();

        final ImageView markerImage = (ImageView) infoWindowContents.findViewById(R.id.pub_image);
        final TextView markerText = (TextView) infoWindowContents.findViewById(R.id.pub_name);

        markerText.setText( pub.getName() );

        String pubImageUrl = (pub.getPhotos() != null && pub.getPhotos().size() > 0)
                            ? pub.getPhotos().get(0)
                            : null;

        // If the pub has no images, show the default image
        if (pubImageUrl == null) {
            ImageManager.getInstance(context).loadImage(
                    R.drawable.default_placeholder,
                    markerImage);
        }

        // If there is an image to show, it's necessary to re-show the info window after download it
        // (because the Info Window is not a live view, it is rendered as a whole image)
        else {

            ImageManager.getInstance(context).loadImage(
                    pubImageUrl,
                    markerImage,
                    R.drawable.error_placeholder,
                    R.drawable.default_placeholder,
                    new ImageManager.ImageLoadListener() {
                        @Override public void onImageLoadError() {}
                        @Override public void onImageLoadCompletion() { refreshInfoWindow(marker); }
                    }
            );
        }

        return infoWindowContents;
    }


    // Hides the Info Window associated to the given marker, and shows it again
    private void refreshInfoWindow(Marker marker) {

        if ( marker.isInfoWindowShown() ) {
            marker.hideInfoWindow();
            marker.showInfoWindow();
        }
    }

}