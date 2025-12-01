package com.example.eventlottery.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.ItemAdminEntrantBinding;
import com.example.eventlottery.model.Profile;

import java.util.Objects;

/**
 * A {@link ListAdapter} for displaying a list of {@link Profile} objects for administrative purposes.
 * It shows each user's name, email, and role, and allows for selection to view or edit details.
 */
public class AdminEntrantAdapter extends ListAdapter<Profile, AdminEntrantAdapter.AdminEntrantViewHolder> {

    /**
     * Interface for handling profile selection events.
     */
    interface Listener {
        /**
         * Called when a profile in the list is selected.
         *
         * @param profile The selected {@link Profile} object.
         */
        void onProfileSelected(@NonNull Profile profile);
    }

    private final Listener listener;

    /**
     * Constructs a new {@link AdminEntrantAdapter}.
     *
     * @param listener The listener to be notified of profile selection events.
     */
    AdminEntrantAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link AdminEntrantViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new AdminEntrantViewHolder that holds a View for a single profile.
     */
    @NonNull
    @Override
    public AdminEntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAdminEntrantBinding binding = ItemAdminEntrantBinding.inflate(inflater, parent, false);
        return new AdminEntrantViewHolder(binding);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method updates the
     * contents of the {@link AdminEntrantViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull AdminEntrantViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * A {@link RecyclerView.ViewHolder} that describes a profile item view and metadata about its place
     * within the RecyclerView.
     */
    class AdminEntrantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemAdminEntrantBinding binding;
        private Profile boundProfile;

        /**
         * Constructs a new {@link AdminEntrantViewHolder}.
         *
         * @param binding The view binding for the item layout.
         */
        AdminEntrantViewHolder(@NonNull ItemAdminEntrantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        /**
         * Binds a {@link Profile} object to the view holder, updating the UI elements.
         *
         * @param profile The profile to display.
         */
        void bind(@NonNull Profile profile) {
            boundProfile = profile;
            binding.adminEntrantName.setText(profile.getName());
            binding.adminEntrantEmail.setText(profile.getEmail());
            binding.adminEntrantRole.setText(profile.getRole().name());
        }

        /**
         * Handles click events on the item view. Notifies the listener if a profile
         * has been bound.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (boundProfile != null && listener != null) {
                listener.onProfileSelected(boundProfile);
            }
        }
    }

    /**
     * A {@link DiffUtil.ItemCallback} for calculating the difference between two non-null {@link Profile} items in a list.
     * This helps the {@link ListAdapter} determine which items have changed, been added, or been removed.
     */
    private static final DiffUtil.ItemCallback<Profile> DIFF_CALLBACK = new DiffUtil.ItemCallback<Profile>() {
        /**
         * Called to check whether two objects represent the same item.
         *
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return True if the two items have the same device ID, false otherwise.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
            return oldItem.getDeviceID().equals(newItem.getDeviceID());
        }

        /**
         * Called to check whether two items have the same data.
         *
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return True if the contents of the items are the same, false otherwise.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getEmail(), newItem.getEmail())
                    && Objects.equals(oldItem.getPhone(), newItem.getPhone())
                    && oldItem.getRole().equals(newItem.getRole());
        }
    };
}
