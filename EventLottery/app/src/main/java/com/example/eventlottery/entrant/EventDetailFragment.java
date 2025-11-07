package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentEventDetailBinding;
import com.example.eventlottery.model.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment that shows details for a single event.
 * Handles joining/leaving waiting lists and admin deletion.
 */
public class EventDetailFragment extends Fragment {

    public static final String ARG_EVENT = "event";

    private FragmentEventDetailBinding binding;
    private Event currentEvent;
    private boolean isAdmin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    private String currentUserDeviceId;
    private EventRepository eventRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the current device ID
        currentUserDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        // Get repository
        eventRepository = RepositoryProvider.getEventRepository();

        // Setup toolbar back button
        binding.eventDetailToolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        // Load event from savedInstanceState or arguments
        Event event = null;
        if (savedInstanceState != null) {
            event = (Event) savedInstanceState.getSerializable(ARG_EVENT);
        }
        if (event == null && getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        }
        if (event == null) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        currentEvent = event;
        // Configure admin delete button
        configAdminButton();

        // Fetch latest event from repository
        Event latestEvent = eventRepository.findEventById(event.getId());
        if (latestEvent != null) {
            event = latestEvent;
        }

        // Bind event details to UI
        bindEvent(event);
        setupActionButtons(event, currentUserDeviceId);
    }

    /**
     * Populates the UI fields with event details.
     */
    private void bindEvent(@NonNull Event event) {
        binding.eventDetailPoster.setImageResource(event.getPosterResId());
        binding.eventDetailTitle.setText(event.getTitle());
        binding.eventDetailOrganizer.setText(getString(R.string.event_detail_organizer_format, event.getOrganizerName()));

        Date date = new Date(event.getStartTimeMillis());
        String dateCopy = dateFormat.format(date);
        String timeCopy = timeFormat.format(date).toLowerCase(Locale.getDefault());
        binding.eventDetailDate.setText(getString(R.string.event_detail_datetime_format, dateCopy, timeCopy));
        binding.eventDetailVenue.setText(event.getVenue());
        binding.eventDetailCapacity.setText(getString(R.string.event_detail_capacity_format, event.getCapacity()));
        binding.eventDetailSpots.setText(getString(R.string.event_detail_spots_format, Math.max(event.getSpotsRemaining(), 0)));
        if (!TextUtils.isEmpty(event.getDescription())) {
            binding.eventDetailDescription.setText(event.getDescription());
        } else {
            binding.eventDetailDescription.setText(R.string.event_detail_description_placeholder);
        }
    }

    /**
     * Shows admin delete button if user is admin.
     */
    private void configAdminButton() {
        if (!isAdmin || binding == null) {
            binding.adminDeleteButton.setVisibility(View.GONE);
            return;
        }
        binding.adminDeleteButton.setVisibility(View.VISIBLE);
        binding.adminDeleteButton.setOnClickListener(v -> {
            if (currentEvent == null) return;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_event_confirm_title)
                    .setMessage(R.string.delete_event_confirm_body)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.delete_event, (dialog, which) -> performDelete())
                    .show();
        });
    }

    /**
     * Deletes current event from repository
     */
    private void performDelete() {
        if (currentEvent == null) return;
        try {
            eventRepository.deleteEvent(currentEvent.getId());
            Toast.makeText(requireContext(), R.string.delete_event_success, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.delete_event_failure, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows or hides the join/leave button depending on registration status
     */
    private void setupActionButtons(@NonNull Event event, String userID) {
        if (event.getStatus() == Event.Status.REG_OPEN) {
            binding.buttonContainer.setVisibility(View.VISIBLE);
            setupButton(event, userID);
        } else {
            binding.buttonContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Configures the join/leave waiting list button
     */
    private void setupButton(@NonNull Event event, String userID) {
        boolean isOnWaitlist = event.isOnWaitingList(userID);

        if (isOnWaitlist) {
            binding.joinEventButton.setText(R.string.leave_waiting_list);
            binding.joinEventButton.setOnClickListener(v -> {
                try {
                    event.leaveWaitingList(userID);
                    eventRepository.updateWaitingList(event.getId(), event.getWaitingList());
                    Toast.makeText(requireContext(), "Left waiting list", Toast.LENGTH_SHORT).show();
                    // Update UI
                    setupButton(event, userID);
                    binding.eventDetailSpots.setText(getString(R.string.event_detail_spots_format, Math.max(event.getSpotsRemaining(), 0)));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Failed to leave waiting list", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            binding.joinEventButton.setText(R.string.join_waiting_list);
            binding.joinEventButton.setOnClickListener(v -> {
                try {
                    event.joinWaitingList(userID);
                    eventRepository.updateWaitingList(event.getId(), event.getWaitingList());
                    Toast.makeText(requireContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
                    // Update UI
                    setupButton(event, userID);
                    binding.eventDetailSpots.setText(getString(R.string.event_detail_spots_format, Math.max(event.getSpotsRemaining(), 0)));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Failed to join waiting list", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentEvent != null) {
            outState.putSerializable(ARG_EVENT, currentEvent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}