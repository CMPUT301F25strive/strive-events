package com.example.eventlottery.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.databinding.ItemWaitingUserBinding;
import com.example.eventlottery.model.Profile;

/**
 * Adapter for waiting list RecyclerView
 */
public class WaitingListAdapter extends ListAdapter<Profile, WaitingListAdapter.ViewHolder> {

    public WaitingListAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * DiffUtil for efficient RecyclerView updates
     *
     */
    private static final DiffUtil.ItemCallback<Profile> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Profile>() {
                @Override
                public boolean areItemsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
                    return oldItem.getDeviceID().equals(newItem.getDeviceID());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWaitingUserBinding binding =
                ItemWaitingUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = getItem(position);
        if (profile != null) {
            holder.bind(profile);
        }
    }

    /**
     * ViewHolder for each item in the RecyclerView
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemWaitingUserBinding binding;

        ViewHolder(ItemWaitingUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Optional: row click listener for future actions
            itemView.setOnClickListener(v -> {
                // Add click actions here if needed
            });
        }

        void bind(Profile profile) {
            // Null-safe binding
            binding.waitingUserName.setText(profile.getName() != null ? profile.getName() : "Unknown User");
            binding.waitingUserEmail.setText(profile.getEmail() != null ? profile.getEmail() : "No email");
            binding.waitingUserPhone.setText(profile.getPhone() != null ? profile.getPhone() : "No phone");

            // Optional: hide email or phone if empty
            binding.waitingUserEmail.setVisibility(profile.getEmail() != null ? View.VISIBLE : View.GONE);
            binding.waitingUserPhone.setVisibility(profile.getPhone() != null ? View.VISIBLE : View.GONE);
        }
    }
}