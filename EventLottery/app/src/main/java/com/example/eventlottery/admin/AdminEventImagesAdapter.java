package com.example.eventlottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.model.Event;

/**
 * A {@link ListAdapter} for displaying a grid of event poster images for administrative review.
 * Each item in the grid shows the event's poster, title, and a button to remove the poster.
 */
public class AdminEventImagesAdapter extends ListAdapter<Event, AdminEventImagesAdapter.ImageViewHolder> {

    /**
     * Interface for handling poster removal events.
     */
    interface Listener {
        /**
         * Called when the remove button for an event's poster is clicked.
         *
         * @param event The {@link Event} whose poster should be removed.
         */
        void onRemovePoster(@NonNull Event event);
    }

    private final Listener listener;

    /**
     * Constructs a new {@link AdminEventImagesAdapter}.
     *
     * @param listener The listener to be notified of poster removal events.
     */
    protected AdminEventImagesAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link ImageViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ImageViewHolder that holds a View for a single event poster.
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method updates the
     * contents of the {@link ImageViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * A {@link RecyclerView.ViewHolder} that describes an event poster item view and metadata
     * about its place within the RecyclerView.
     */
    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        ImageButton removeButton;

        /**
         * Constructs a new {@link ImageViewHolder}.
         *
         * @param itemView The view for the item layout.
         */
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.adminPosterImage);
            title = itemView.findViewById(R.id.adminPosterTitle);
            removeButton = itemView.findViewById(R.id.adminPosterRemoveButton);
        }

        /**
         * Binds an {@link Event} object to the view holder, updating the UI elements.
         *
         * @param event The event to display.
         */
        void bind(Event event) {
            title.setText(event.getTitle());
            // Use Glide to load the event poster image
            Glide.with(poster.getContext())
                    .load(event.getPosterUrl())
                    .placeholder(R.drawable.event_image_placeholder)
                    .error(R.drawable.event_image_placeholder)
                    .centerCrop()
                    .into(poster);

            // Set a click listener on the remove button to trigger the callback
            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemovePoster(event);
                }
            });
        }
    }

    /**
     * A {@link DiffUtil.ItemCallback} for calculating the difference between two non-null {@link Event} items in a list.
     * This helps the {@link ListAdapter} determine which items have changed, been added, or been removed efficiently.
     */
    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Event>() {
                /**
                 * Called to check whether two objects represent the same item.
                 *
                 * @param oldItem The item in the old list.
                 * @param newItem The item in the new list.
                 * @return True if the two items have the same ID, false otherwise.
                 */
                @Override
                public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    // Items are considered the same if they have the same unique ID.
                    return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                }

                /**
                 * Called to check whether two items have the same data.
                 * This is used to detect if an item's content has changed.
                 *
                 * @param oldItem The item in the old list.
                 * @param newItem The item in the new list.
                 * @return True if the contents of the items are the same, false otherwise.
                 */
                @Override
                public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    // Items' contents are considered the same if their poster URL, title,
                    // and organizer name are all equal.
                    return String.valueOf(oldItem.getPosterUrl())
                            .equals(String.valueOf(newItem.getPosterUrl()))
                            && String.valueOf(oldItem.getTitle()).equals(String.valueOf(newItem.getTitle()))
                            && String.valueOf(oldItem.getOrganizerName())
                            .equals(String.valueOf(newItem.getOrganizerName()));
                }
            };
}
