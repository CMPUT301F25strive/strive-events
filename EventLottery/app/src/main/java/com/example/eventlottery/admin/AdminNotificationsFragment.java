package com.example.eventlottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.data.NotificationRepository;
import com.example.eventlottery.databinding.FragmentAdminNotificationsBinding;
import com.example.eventlottery.model.NotificationLogEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminNotificationsFragment extends Fragment implements AdminNotificationAdapter.Listener {

    private FragmentAdminNotificationsBinding binding;
    private AdminNotificationAdapter adapter;
    private NotificationRepository notificationRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminNotificationAdapter(this);
        binding.notificationList.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        binding.notificationList.setAdapter(adapter);
        binding.notificationList.setHasFixedSize(true);

        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        notificationRepository = new NotificationRepository();
        notificationRepository.observeOrganizerNotifications()
                .observe(getViewLifecycleOwner(), this::bindNotifications);
    }

    private void bindNotifications(@Nullable List<NotificationLogEntry> notifications) {
        List<NotificationLogEntry> safeList =
                notifications != null ? notifications : Collections.emptyList();
        adapter.submitList(safeList);

        Map<String, Integer> counts = new HashMap<>();
        for (NotificationLogEntry entry : safeList) {
            if (entry.isFlagged()) {
                int updated = counts.containsKey(entry.getSenderId())
                        ? counts.get(entry.getSenderId()) + 1
                        : 1;
                counts.put(entry.getSenderId(), updated);
            }
        }
        adapter.setFlagCountMap(counts);

        binding.emptyView.setVisibility(safeList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onFlagTapped(@NonNull NotificationLogEntry entry) {
        boolean nextFlagState = !entry.isFlagged();
        String title = getString(nextFlagState
                ? com.example.eventlottery.R.string.admin_notification_flag_title
                : com.example.eventlottery.R.string.admin_notification_unflag_title);
        String message = getString(nextFlagState
                ? com.example.eventlottery.R.string.admin_notification_flag_body
                : com.example.eventlottery.R.string.admin_notification_unflag_body);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(nextFlagState
                        ? com.example.eventlottery.R.string.admin_notification_flag
                        : com.example.eventlottery.R.string.admin_notification_unflag,
                        (dialog, which) -> notificationRepository.setFlagStatus(entry.getId(), nextFlagState))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationRepository != null) {
            notificationRepository.clear();
        }
        binding = null;
    }
}
