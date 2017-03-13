package io.keepcoding.pickandgol.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.fragment.EventListFragment;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;


/**
 * This is an adapter to manage an Event list (by using a RecyclerView)
 */
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    private Context context;
    private EventAggregate events;
    private EventListFragment.EventListListener listener;
    private ImageManager im;
    private LayoutInflater inflater;


    public EventListAdapter(Context context, EventAggregate events) {

        this.context = context;
        this.events = events;

        this.im = ImageManager.getInstance(context);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = inflater.inflate(R.layout.item_event, parent, false);
        EventViewHolder holder = new EventViewHolder(layoutView);

        return holder;
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, final int position) {

        final Event event = events.get(position);

        holder.bindData(event);

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onItemClicked(event, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setOnEventClickListener(@NonNull final EventListFragment.EventListListener listener) {
        this.listener = listener;
    }

    public void addMoreItems(EventAggregate moreEvents) {
        events.addElements(moreEvents);
    }


    // Auxiliary class that represents the view holder for an event
    class EventViewHolder extends RecyclerView.ViewHolder {

        private View view;

        TextView eventTitle;
        TextView eventDate;
        ImageView eventImage;

        public EventViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;

            eventTitle = (TextView) itemView.findViewById(R.id.item_event_title);
            eventDate = (TextView) itemView.findViewById(R.id.item_event_date);
            eventImage = (ImageView) itemView.findViewById(R.id.item_event_image);
        }

        public void bindData(Event event) {

            String title = event.getName();
            String date = event.getDate().toString();
            String imgUrl = event.getPhotoUrl();

            eventTitle.setText(title);
            eventDate.setText(date);

            if (imgUrl != null)
                im.loadImage(imgUrl, eventImage, R.drawable.error_placeholder);
            else
                im.loadImage(R.drawable.default_placeholder, eventImage);
        }

        public View getView() {
            return view;
        }
    }
}
