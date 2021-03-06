package io.keepcoding.pickandgol.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.activity.PubSelectorActivity;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.view.PubListListener;

import static io.keepcoding.pickandgol.adapter.PubListAdapter.LayoutType.ROWS;
import static io.keepcoding.pickandgol.adapter.PubListAdapter.LayoutType.ROWS_WITH_DETAIL_BUTTON;


/**
 * This is an adapter to manage a Pub list by using a RecyclerView.
 * It supports different layouts for the recycler: cell grid and "classic" row list
 * (including a special layout where the rows have a 'show detail' button).
 */
public class PubListAdapter extends RecyclerView.Adapter<PubListAdapter.PubViewHolder> {

    // Available types of layout to represent the Pub list
    public static enum LayoutType {
        ROWS,
        ROWS_WITH_DETAIL_BUTTON,
        CELLS
    }

    // Layouts for the list elements (depending on the chosen layout type)
    // (make sure they exist and that both have the same view names)
    private static final int ROW_LAYOUT_ID = R.layout.row_pub;
    private static final int ROW_WITH_DETAIL_BUTTON_LAYOUT_ID = R.layout.row_pub_with_detail_button;
    private static final int CELL_LAYOUT_ID = R.layout.item_pub;


    private Context context;
    private PubAggregate pubs;
    private int layoutId;
    private PubListListener listener;
    private ImageManager im;
    private LayoutInflater inflater;


    public PubListAdapter(Context context, PubAggregate pubs, LayoutType type) {

        this.context = context;
        this.pubs = pubs;

        if (type == ROWS)
            this.layoutId = ROW_LAYOUT_ID;

        else if (type == ROWS_WITH_DETAIL_BUTTON)
            this.layoutId = ROW_WITH_DETAIL_BUTTON_LAYOUT_ID;

        else
            this.layoutId = CELL_LAYOUT_ID;

        this.im = ImageManager.getInstance(context);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public PubViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(layoutId, parent, false);
        PubViewHolder holder = new PubViewHolder(layoutView);

        return holder;
    }

    @Override
    public void onBindViewHolder(PubViewHolder holder, final int position) {

        final Pub pub = pubs.get(position);

        holder.bindData(pub);

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onPubClicked(pub, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pubs.size();
    }

    public void setOnPubClickListener(@NonNull final PubListListener listener) {
        this.listener = listener;
    }

    public void addMoreItems(PubAggregate morePubs) {
        pubs.addElements(morePubs);
    }


    // Auxiliary class that represents the view holder for a Pub
    class PubViewHolder extends RecyclerView.ViewHolder {

        private View view;

        TextView pubName;
        ImageView pubImage;
        Button pubButton;

        public PubViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;

            pubName = (TextView) itemView.findViewById(R.id.pub_name);
            pubImage = (ImageView) itemView.findViewById(R.id.pub_image);

            // NOTE: depending on the chosen layout, the button might not exist
            pubButton = (Button) itemView.findViewById(R.id.pub_button);
        }

        public void bindData(final Pub pub) {

            String name = pub.getName();
            pubName.setText(name);

            String imgUrl = null;

            if (pub.getPhotos() != null && pub.getPhotos().size() > 0)
                imgUrl = pub.getPhotos().get(0);

            if (imgUrl != null)
                im.loadImage(imgUrl, pubImage, R.drawable.error_placeholder);
            else
                im.loadImage(R.drawable.default_placeholder, pubImage);

            // If we are using a layout with a detail button, assign a listener to it
            if (pubButton != null) {
                pubButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Navigator.fromPubSelectorActivityToPubDetailActivity(
                                (PubSelectorActivity) context,
                                pub
                        );
                    }
                });
            }
        }

        public View getView() {
            return view;
        }
    }
}
