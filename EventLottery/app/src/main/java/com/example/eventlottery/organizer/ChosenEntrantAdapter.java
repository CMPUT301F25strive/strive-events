package com.example.eventlottery.organizer;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.ItemChosenEntrantBinding;

import java.util.Objects;

/**
 * Simple adapter that renders the invited entrants list for organizers.
 */
public class ChosenEntrantAdapter
        extends ListAdapter<ChosenEntrantAdapter.Row, ChosenEntrantAdapter.ViewHolder> {

    public ChosenEntrantAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChosenEntrantBinding binding = ItemChosenEntrantBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // -----------------------------------------------------------------------------------------

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemChosenEntrantBinding binding;

        ViewHolder(@NonNull ItemChosenEntrantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull Row row) {
            Context context = binding.getRoot().getContext();

            binding.entrantName.setText(!TextUtils.isEmpty(row.displayName)
                    ? row.displayName
                    : context.getString(R.string.unknown));

            if (TextUtils.isEmpty(row.email)) {
                binding.entrantEmail.setVisibility(android.view.View.GONE);
            } else {
                binding.entrantEmail.setVisibility(android.view.View.VISIBLE);
                binding.entrantEmail.setText(row.email);
            }

            if (TextUtils.isEmpty(row.phone)) {
                binding.entrantPhone.setVisibility(android.view.View.GONE);
            } else {
                binding.entrantPhone.setVisibility(android.view.View.VISIBLE);
                binding.entrantPhone.setText(row.phone);
            }

            String deviceDisplay = !TextUtils.isEmpty(row.deviceId) ? row.deviceId : "--";
            binding.deviceIdLabel.setText(
                    context.getString(R.string.device_id_label, deviceDisplay)
            );

            binding.statusChip.setText(getStatusLabel(row.status, context));
            @ColorRes int chipColor = getStatusColor(row.status);
            ViewCompat.setBackgroundTintList(
                    binding.statusChip,
                    ContextCompat.getColorStateList(context, chipColor)
            );
        }

        @NonNull
        private String getStatusLabel(@NonNull Row.Status status, @NonNull Context context) {
            switch (status) {
                case ACCEPTED:
                    return context.getString(R.string.chosen_status_accepted);
                case DECLINED:
                    return context.getString(R.string.chosen_status_declined);
                case PENDING:
                default:
                    return context.getString(R.string.chosen_status_pending);
            }
        }

        @ColorRes
        private int getStatusColor(@NonNull Row.Status status) {
            switch (status) {
                case ACCEPTED:
                    return R.color.chosen_status_accepted;
                case DECLINED:
                    return R.color.chosen_status_declined;
                case PENDING:
                default:
                    return R.color.chosen_status_pending;
            }
        }
    }

    // -----------------------------------------------------------------------------------------

    public static class Row {
        public enum Status { PENDING, ACCEPTED, DECLINED }

        public final String deviceId;
        public final String displayName;
        public final String email;
        public final String phone;
        public final Status status;

        public Row(@NonNull String deviceId,
                   String displayName,
                   String email,
                   String phone,
                   @NonNull Status status) {
            this.deviceId = deviceId;
            this.displayName = displayName;
            this.email = email;
            this.phone = phone;
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Row row = (Row) o;
            return Objects.equals(deviceId, row.deviceId) &&
                    Objects.equals(displayName, row.displayName) &&
                    Objects.equals(email, row.email) &&
                    Objects.equals(phone, row.phone) &&
                    status == row.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(deviceId, displayName, email, phone, status);
        }
    }

    private static final DiffUtil.ItemCallback<Row> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Row>() {
                @Override
                public boolean areItemsTheSame(@NonNull Row oldItem, @NonNull Row newItem) {
                    return Objects.equals(oldItem.deviceId, newItem.deviceId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Row oldItem, @NonNull Row newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
