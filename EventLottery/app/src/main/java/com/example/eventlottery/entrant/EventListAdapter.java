package com.example.eventlottery.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.databinding.ItemEventCardBinding;
import com.example.eventlottery.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * recycler adapter for entrant event list.
 */
public class EventListAdapter extends ListAdapter<Event, EventListAdapter.EventViewHolder> {

    public interface Listener {
        void onEventSelected(@NonNull Event event);
    }

    private final Listener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public EventListAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEventCardBinding binding = ItemEventCardBinding.inflate(inflater, parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = getItem(position);
        holder.bind(event);
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
            binding.eventPoster.setImageResource(event.getPosterResId());
            binding.eventTitle.setText(event.getTitle());
            binding.eventOrganizer.setText(String.format(Locale.getDefault(), "with %s", event.getOrganizerName()));

            Date date = new Date(event.getStartTimeMillis());
            binding.eventDate.setText(dateFormat.format(date));
            String timeCopy = timeFormat.format(date).toLowerCase(Locale.getDefault());
            binding.eventTime.setText(timeCopy);
            binding.eventVenue.setText(event.getVenue());
            binding.eventStatus.setText(buildStatusCopy(event));
        }

        private String buildStatusCopy(@NonNull Event event) {
            if (event.getStatus() == Event.Status.REG_OPEN) {
                if (event.getSpotsRemaining() > 0) {
                    return String.format(Locale.getDefault(), "%d spots left", event.getSpotsRemaining());
                }
                return "waitlist almost full";
            }
            return event.getStatus().name();
        }

        @Override
        public void onClick(View v) {
            if (boundEvent != null) {
                listener.onEventSelected(boundEvent);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            return oldItem.equals(newItem);
        }
    };
}
