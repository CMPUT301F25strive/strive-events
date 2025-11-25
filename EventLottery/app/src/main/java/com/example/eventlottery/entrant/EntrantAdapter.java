package com.example.eventlottery.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EntrantAdapter extends ListAdapter<Profile, EntrantAdapter.EntrantViewHolder> {

    interface Listener {
        void onProfileSelected(@NonNull Profile profile);
    }

    private final Listener listener;
    private final List<Profile> selectedProfiles = new ArrayList<>();

    public EntrantAdapter(@NonNull Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_notification_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public List<Profile> getSelectedProfiles() {
        return selectedProfiles;
    }

    /**
     * This method toggles the selection state of a profile.
     * @param profile the profile to select or deselect
     */
    public void toggleSelection(Profile profile) {
        if (selectedProfiles.contains(profile)) {
            selectedProfiles.remove(profile);
        } else {
            selectedProfiles.add(profile);
        }
        notifyItemChanged(getCurrentList().indexOf(profile));
    }

    /**
     * This methods clears the checkboxes after sending the notification
     */
    public void clearSelection() {
        selectedProfiles.clear();
        notifyDataSetChanged();
    }

    class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        CheckBox checkBox;
        Profile profile;

        EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.entrantNameText);
            checkBox = itemView.findViewById(R.id.entrantCheckBox);

            itemView.setOnClickListener(v -> {
                if (profile != null && listener != null) {
                    listener.onProfileSelected(profile);
                }
            });

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (profile == null) return;
                if (isChecked) {
                    if (!selectedProfiles.contains(profile)) selectedProfiles.add(profile);
                } else {
                    selectedProfiles.remove(profile);
                }
            });
        }

        void bind(Profile profile) {
            this.profile = profile;
            nameText.setText(profile.getName());
            checkBox.setChecked(selectedProfiles.contains(profile));
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
                    && Objects.equals(oldItem.getEmail(), newItem.getEmail());
        }
    };
}