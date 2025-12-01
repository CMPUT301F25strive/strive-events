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
 * Adapter displaying organizer notifications for admins.
 */
public class AdminNotificationAdapter extends ListAdapter<NotificationLogEntry, AdminNotificationAdapter.NotificationViewHolder> {

    interface Listener {
        void onFlagTapped(@NonNull NotificationLogEntry entry);
    }

    private final Listener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    private Map<String, Integer> flagCountMap = new HashMap<>();

    AdminNotificationAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    void setFlagCountMap(@NonNull Map<String, Integer> counts) {
        flagCountMap = new HashMap<>(counts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView organizerText;
        private final TextView eventText;
        private final TextView timestampText;
        private final TextView messageText;
        private final TextView flagBadge;
        private final TextView repeatedBadge;
        private final Button flagButton;

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

    private static final DiffUtil.ItemCallback<NotificationLogEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NotificationLogEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLogEntry oldItem,
                                               @NonNull NotificationLogEntry newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

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
