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
 * Grid adapter showing event poster thumbnails for admins.
 */
public class AdminEventImagesAdapter extends ListAdapter<Event, AdminEventImagesAdapter.ImageViewHolder> {

    interface Listener {
        void onRemovePoster(@NonNull Event event);
    }

    private final Listener listener;

    protected AdminEventImagesAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        ImageButton removeButton;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.adminPosterImage);
            title = itemView.findViewById(R.id.adminPosterTitle);
            removeButton = itemView.findViewById(R.id.adminPosterRemoveButton);
        }

        void bind(Event event) {
            title.setText(event.getTitle());
            Glide.with(poster.getContext())
                    .load(event.getPosterUrl())
                    .placeholder(R.drawable.event_image_placeholder)
                    .error(R.drawable.event_image_placeholder)
                    .centerCrop()
                    .into(poster);

            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemovePoster(event);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Event>() {
                @Override
                public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    return String.valueOf(oldItem.getPosterUrl())
                            .equals(String.valueOf(newItem.getPosterUrl()))
                            && String.valueOf(oldItem.getTitle()).equals(String.valueOf(newItem.getTitle()))
                            && String.valueOf(oldItem.getOrganizerName())
                            .equals(String.valueOf(newItem.getOrganizerName()));
                }
            };
}
