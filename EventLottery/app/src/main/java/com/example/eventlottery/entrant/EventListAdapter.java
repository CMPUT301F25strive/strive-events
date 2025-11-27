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
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.ItemEventCardBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class EventListAdapter extends ListAdapter<Event, EventListAdapter.EventViewHolder> {

    public interface Listener {
        void onEventSelected(@NonNull Event event);
    }

    private final Listener listener;
    private final ProfileRepository profileRepository;
    private final Map<String, String> organizerCache = new HashMap<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public EventListAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.profileRepository = RepositoryProvider.getProfileRepository();
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

            // === POSTER + WATERMARK ===
            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(event.getPosterUrl())
                        .placeholder(R.drawable.event_image_placeholder)
                        .into(binding.eventPoster);
                binding.posterWatermark.setVisibility(View.GONE); // hide watermark if poster exists
            } else {
                binding.eventPoster.setImageResource(R.drawable.event_image_placeholder);
                binding.posterWatermark.setVisibility(View.VISIBLE); // show watermark if no poster
            }

            // === TITLE ===
            binding.eventTitle.setText(event.getTitle());

            // === ORGANIZER ===
            String organizerId = event.getOrganizerId();
            if (organizerId == null || organizerId.isEmpty()) {
                binding.eventOrganizer.setText("UNKNOWN");
            } else if (organizerCache.containsKey(organizerId)) {
                binding.eventOrganizer.setText("Organizer: " + organizerCache.get(organizerId));
            } else {
                profileRepository.findUserById(organizerId, new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(Profile profile) {
                        String name = profile != null ? profile.getName() : "UNKNOWN";
                        organizerCache.put(organizerId, name.toUpperCase());
                        binding.eventOrganizer.setText(name.toUpperCase());
                    }

                    @Override
                    public void onDeleted() {
                        organizerCache.put(organizerId, "Deleted User");
                        binding.eventOrganizer.setText("DELETED USER");
                    }

                    @Override
                    public void onError(String message) {
                        binding.eventOrganizer.setText("UNKNOWN");
                    }
                });
            }

            // === DATE & TIME ===
            Date date = new Date(event.getEventStartTimeMillis());
            binding.eventDate.setText(dateFormat.format(date));
            binding.eventTime.setText(timeFormat.format(date).toLowerCase(Locale.getDefault()));

            // === VENUE ===
            binding.eventVenue.setText(event.getVenue());

            // === STATUS ===
            binding.eventStatus.setText(""); // Can add status if needed

            Log.d("EventAdapter", "Event: " + event.getTitle() + ", millis: " + event.getEventStartTimeMillis());
        }

        @Override
        public void onClick(View v) {
            if (boundEvent != null) listener.onEventSelected(boundEvent);
        }
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getVenue().equals(newItem.getVenue()) &&
                    oldItem.getEventStartTimeMillis() == newItem.getEventStartTimeMillis() &&
                    String.valueOf(oldItem.getPosterUrl()).equals(String.valueOf(newItem.getPosterUrl()));
        }
    };
}