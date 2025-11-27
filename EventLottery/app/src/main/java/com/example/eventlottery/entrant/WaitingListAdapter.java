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

public class WaitingListAdapter extends ListAdapter<Profile, WaitingListAdapter.ViewHolder> {

    public WaitingListAdapter() {
        super(DIFF_CALLBACK);
    }

    // DiffUtil for efficient RecyclerView updates
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWaitingUserBinding binding =
                ItemWaitingUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile profile = getItem(position);
        if (profile != null) {
            holder.bind(profile);
        }
    }

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