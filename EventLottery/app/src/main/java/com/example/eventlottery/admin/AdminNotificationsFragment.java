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

/**
 * An admin-only screen that displays a list of notifications sent by organizers.
 * This fragment allows administrators to review all notifications, see which organizers
 * are sending multiple messages, and flag or unflag specific notifications for further review.
 */
public class AdminNotificationsFragment extends Fragment implements AdminNotificationAdapter.Listener {

    /** View binding for the fragment's layout. */
    private FragmentAdminNotificationsBinding binding;
    /** Adapter for the RecyclerView that displays the list of notifications. */
    private AdminNotificationAdapter adapter;
    /** Repository for accessing and modifying notification data. */
    private NotificationRepository notificationRepository;

    /**
     * Inflates the fragment's view and initializes view binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The root view of the inflated layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view has been created. Initializes the RecyclerView, adapter, repository,
     * and sets up UI listeners. It begins observing the notification logs to populate the list.
     *
     * @param view The View object.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the adapter and configure the RecyclerView
        adapter = new AdminNotificationAdapter(this);
        binding.notificationList.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        binding.notificationList.setAdapter(adapter);
        binding.notificationList.setHasFixedSize(true);

        // Set up the back button to navigate to the previous screen
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Initialize the repository and observe notification data
        notificationRepository = new NotificationRepository();
        notificationRepository.observeOrganizerNotifications()
                .observe(getViewLifecycleOwner(), this::bindNotifications);
    }

    /**
     * Processes the list of notifications from the repository. Submits the list to the adapter
     * and calculates the frequency of notifications per sender to highlight repeat senders.
     *
     * @param notifications The list of {@link NotificationLogEntry} objects from the observer.
     */
    private void bindNotifications(@Nullable List<NotificationLogEntry> notifications) {
        List<NotificationLogEntry> safeList =
                notifications != null ? notifications : Collections.emptyList();
        adapter.submitList(safeList);

        // Count flagged notifications per sender to identify repeated senders
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

        // Show or hide the empty state view based on the list content
        binding.emptyView.setVisibility(safeList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Handles the tap event on the flag/unflag button for a notification entry.
     * It shows a confirmation dialog before updating the flag status in the repository.
     *
     * @param entry The non-null {@link NotificationLogEntry} that was acted upon.
     */
    @Override
    public void onFlagTapped(@NonNull NotificationLogEntry entry) {
        boolean nextFlagState = !entry.isFlagged();
        // Determine the dialog title and message based on whether we are flagging or unflagging
        String title = getString(nextFlagState
                ? com.example.eventlottery.R.string.admin_notification_flag_title
                : com.example.eventlottery.R.string.admin_notification_unflag_title);
        String message = getString(nextFlagState
                ? com.example.eventlottery.R.string.admin_notification_flag_body
                : com.example.eventlottery.R.string.admin_notification_unflag_body);

        // Display a confirmation dialog to the admin
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

    /**
     * Called when the view is destroyed. Cleans up resources to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear the repository to remove Firestore listeners
        if (notificationRepository != null) {
            notificationRepository.clear();
        }
        // Nullify the binding to prevent memory leaks
        binding = null;
    }
}
