package com.example.eventlottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.model.NotificationLogEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A {@link ListAdapter} for displaying a list of {@link NotificationLogEntry} items for administrators.
 * This adapter shows details about each notification, such as the sender, event, message, and timestamp.
 * It also provides functionality for admins to flag or unflag notifications and highlights repeat senders.
 */
public class AdminNotificationAdapter extends ListAdapter<NotificationLogEntry, AdminNotificationAdapter.NotificationViewHolder> {

    /**
     * Interface for handling interactions with notification items, specifically flagging.
     */
    interface Listener {
        /**
         * Called when the flag/unflag button for a notification entry is tapped.
         *
         * @param entry The {@link NotificationLogEntry} that was acted upon.
         */
        void onFlagTapped(@NonNull NotificationLogEntry entry);
    }

    private final Listener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    private Map<String, Integer> flagCountMap = new HashMap<>();

    /**
     * Constructs a new {@link AdminNotificationAdapter}.
     *
     * @param listener The listener to be notified of user actions, such as flagging.
     */
    AdminNotificationAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * Updates the map used to track how many notifications have been sent by each organizer.
     * This is used to display a "repeated" badge for organizers who send multiple notifications.
     *
     * @param counts A map where the key is the sender ID and the value is the count of notifications.
     */
    void setFlagCountMap(@NonNull Map<String, Integer> counts) {
        flagCountMap = new HashMap<>(counts);
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link NotificationViewHolder} to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new NotificationViewHolder that holds a View for a single notification log entry.
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * A {@link RecyclerView.ViewHolder} that describes a notification log item view.
     */
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView organizerText;
        private final TextView eventText;
        private final TextView timestampText;
        private final TextView messageText;
        private final TextView flagBadge;
        private final TextView repeatedBadge;
        private final Button flagButton;

        /**
         * Constructs a new {@link NotificationViewHolder}.
         *
         * @param itemView The view for the item layout.
         */
        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            organizerText = itemView.findViewById(R.id.notificationOrganizer);
            eventText = itemView.findViewById(R.id.notificationEvent);
            timestampText = itemView.findViewById(R.id.notificationTimestamp);
            messageText = itemView.findViewById(R.id.notificationMessage);
            flagBadge = itemView.findViewById(R.id.notificationFlagBadge);
            repeatedBadge = itemView.findViewById(R.id.notificationRepeatBadge);
            flagButton = itemView.findViewById(R.id.notificationFlagButton);
        }

        /**
         * Binds a {@link NotificationLogEntry} object to the view holder, updating the UI.
         *
         * @param entry The notification log entry to display.
         */
        void bind(NotificationLogEntry entry) {
            String organizerName = entry.getSenderName();
            if (organizerName == null || organizerName.trim().isEmpty()) {
                organizerName = itemView.getContext().getString(R.string.device_id_label, entry.getSenderId());
            }
            organizerText.setText(organizerName);

            String eventLabel = entry.getEventTitle();
            if (eventLabel == null || eventLabel.trim().isEmpty()) {
                eventLabel = entry.getEventId() != null ? entry.getEventId() :
                        itemView.getContext().getString(R.string.unknown);
            }
            eventText.setText(itemView.getContext().getString(
                    R.string.admin_notification_event_format,
                    eventLabel
            ));

            Date sentDate = entry.getSentAt();
            timestampText.setText(itemView.getContext().getString(
                    R.string.admin_notification_sent_format,
                    dateFormat.format(sentDate)
            ));

            messageText.setText(entry.getMessage());

            flagBadge.setVisibility(entry.isFlagged() ? View.VISIBLE : View.GONE);

            Integer flagCount = flagCountMap.get(entry.getSenderId());
            boolean repeated = flagCount != null && flagCount > 1;
            repeatedBadge.setVisibility(repeated ? View.VISIBLE : View.GONE);

            flagButton.setText(entry.isFlagged()
                    ? R.string.admin_notification_unflag
                    : R.string.admin_notification_flag);
            flagButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFlagTapped(entry);
                }
            });
        }
    }

    /**
     * A {@link DiffUtil.ItemCallback} for calculating the difference between two {@link NotificationLogEntry} items.
     * This allows the {@link ListAdapter} to efficiently update the list.
     */
    private static final DiffUtil.ItemCallback<NotificationLogEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NotificationLogEntry>() {
                /**
                 * Checks if two items represent the same object.
                 *
                 * @param oldItem The item in the old list.
                 * @param newItem The item in the new list.
                 * @return True if the items have the same ID, false otherwise.
                 */
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLogEntry oldItem,
                                               @NonNull NotificationLogEntry newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                /**
                 * Checks if the contents of two items are the same.
                 *
                 * @param oldItem The item in the old list.
                 * @param newItem The item in the new list.
                 * @return True if the items' contents are identical, false otherwise.
                 */
                @Override
                public boolean areContentsTheSame(@NonNull NotificationLogEntry oldItem,
                                                  @NonNull NotificationLogEntry newItem) {
                    return oldItem.isFlagged() == newItem.isFlagged()
                            && oldItem.getMessage().equals(newItem.getMessage())
                            && String.valueOf(oldItem.getEventTitle())
                            .equals(String.valueOf(newItem.getEventTitle()))
                            && String.valueOf(oldItem.getSenderName())
                            .equals(String.valueOf(newItem.getSenderName()));
                }
            };
}
