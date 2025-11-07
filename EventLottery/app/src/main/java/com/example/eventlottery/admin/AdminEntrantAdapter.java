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
 * RecyclerView adapter that lists all entrants for admin moderation.
 */
public class AdminEntrantAdapter extends ListAdapter<Profile, AdminEntrantAdapter.AdminEntrantViewHolder> {

    interface Listener {
        void onProfileSelected(@NonNull Profile profile);
    }

    private final Listener listener;

    AdminEntrantAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminEntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAdminEntrantBinding binding = ItemAdminEntrantBinding.inflate(inflater, parent, false);
        return new AdminEntrantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEntrantViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class AdminEntrantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemAdminEntrantBinding binding;
        private Profile boundProfile;

        AdminEntrantViewHolder(@NonNull ItemAdminEntrantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        void bind(@NonNull Profile profile) {
            boundProfile = profile;
            binding.adminEntrantName.setText(profile.getName());
            binding.adminEntrantEmail.setText(profile.getEmail());
            if (profile.getRole() == Profile.Role.USER) {
                binding.adminEntrantRole.setVisibility(View.GONE);
            } else {
                binding.adminEntrantRole.setVisibility(View.VISIBLE);
                String roleCopy = binding.getRoot().getResources()
                        .getString(R.string.admin_entrant_role_format, profile.getRole().name());
                binding.adminEntrantRole.setText(roleCopy);
            }
        }

        @Override
        public void onClick(View v) {
            if (boundProfile != null && listener != null) {
                listener.onProfileSelected(boundProfile);
            }
        }
    }

    private static final DiffUtil.ItemCallback<Profile> DIFF_CALLBACK = new DiffUtil.ItemCallback<Profile>() {
        @Override
        public boolean areItemsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
            return oldItem.getDeviceID().equals(newItem.getDeviceID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getEmail(), newItem.getEmail())
                    && Objects.equals(oldItem.getPhone(), newItem.getPhone())
                    && oldItem.getRole() == newItem.getRole();
        }
    };
}
