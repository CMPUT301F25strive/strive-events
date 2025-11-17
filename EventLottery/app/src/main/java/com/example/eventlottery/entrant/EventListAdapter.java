package com.example.eventlottery.entrant;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.databinding.ItemEventCardBinding;
import com.example.eventlottery.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventListAdapter extends ListAdapter<Event, EventListAdapter.EventViewHolder> {

    public interface Listener {
        void onEventSelected(@NonNull Event event);
    }

    private final Listener listener;

    private final SimpleDateFormat outputDateFormat =
            new SimpleDateFormat("EEE, MMM d yyyy • h:mm a", Locale.getDefault());

    public EventListAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventCardBinding binding = ItemEventCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemEventCardBinding binding;
        private Event boundEvent;

        EventViewHolder(@NonNull ItemEventCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }
        void bind(@NonNull Event event) {
            boundEvent = event;

            // === POSTER ===
            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(event.getPosterUrl())
                        .into(binding.eventPoster);
            } else {
                binding.eventPoster.setImageResource(R.drawable.event_image_placeholder);
            }

            // === TITLE ===
            binding.eventTitle.setText(event.getTitle());

            // === ORGANIZER ===
            binding.eventOrganizer.setText(event.getOrganizerName() != null ? "with " + event.getOrganizerName() : "");

            // === DATE & TIME ===
            Date date = new Date(event.getStartTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

            binding.eventDate.setText(dateFormat.format(date));
            binding.eventTime.setText(timeFormat.format(date).toLowerCase(Locale.getDefault()));

            // === VENUE ===
            binding.eventVenue.setText(event.getVenue());

            // === STATUS ===
            binding.eventStatus.setText(""); // Or use a status string if needed
            Log.d("EventAdapter", "Event: " + event.getTitle() + ", millis: " + event.getStartTimeMillis());
        }

        @Override
        public void onClick(View v) {
            if (boundEvent != null) {
                listener.onEventSelected(boundEvent);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Event>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Event oldItem,
                        @NonNull Event newItem
                ) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Event oldItem,
                        @NonNull Event newItem
                ) {
                    // Compare meaningful fields only
                    return oldItem.getTitle().equals(newItem.getTitle()) &&
                            oldItem.getVenue().equals(newItem.getVenue()) &&
                            oldItem.getStartTimeMillis() == newItem.getStartTimeMillis() &&
                            String.valueOf(oldItem.getPosterUrl())
                                    .equals(String.valueOf(newItem.getPosterUrl()));
                }
            };
}