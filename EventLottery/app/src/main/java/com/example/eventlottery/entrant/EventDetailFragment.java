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
import com.example.eventlottery.admin.AdminGate;
import com.example.eventlottery.WaitingListController;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentEventDetailBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailFragment extends Fragment {

    public static final String ARG_EVENT = "event";

    private FragmentEventDetailBinding binding;
    private Event currentEvent;
    private boolean isAdmin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    private String currentUserDeviceId;
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;

    private WaitingListController waitingListController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Probably we should get the device ID from DeviceIdentityService class
        // Get device ID
        currentUserDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        // Get repositories
        eventRepository = RepositoryProvider.getEventRepository();
        profileRepository = RepositoryProvider.getProfileRepository();

        binding.eventDetailToolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailFragment.this).popBackStack());

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
        determineAdminStatus();

        // Get the latest event
        Event latestEvent = eventRepository.findEventById(event.getId());
        if (latestEvent != null) {
            event = latestEvent;
        }

        bindEvent(event);

        final String eventId = event.getId();
        eventRepository.observeEvents().observe(getViewLifecycleOwner(), events -> {
            Event updated = eventRepository.findEventById(eventId);
            if (updated != null) {
                bindEvent(updated);
                setupActionButtons(updated, currentUserDeviceId);
            }
        });
        waitingListController = new WaitingListController(
                eventRepository,
                profileRepository
        );
        setupActionButtons(event, currentUserDeviceId);
    }

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
        binding.eventDetailWaitingListCount.setText(getString(R.string.event_detail_waiting_list_count_format, event.getWaitingListSize()));
        if (!TextUtils.isEmpty(event.getDescription())) {
            binding.eventDetailDescription.setText(event.getDescription());
        } else {
            binding.eventDetailDescription.setText(R.string.event_detail_description_placeholder);
        }
    }

    private void configAdminButton() {
        if (!isAdmin || binding == null) {
            binding.adminDeleteButton.setVisibility(View.GONE);
            return;
        }
        binding.adminDeleteButton.setVisibility(View.VISIBLE);
        binding.adminDeleteButton.setOnClickListener(v -> {
            if (currentEvent == null) {
                return;
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_event_confirm_title)
                    .setMessage(R.string.delete_event_confirm_body)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.delete_event, (dialog, which) -> performDelete())
                    .show();
        });
    }

    private void performDelete() {
        EventRepository repository = RepositoryProvider.getEventRepository();
        if (currentEvent == null) return;
        try {
            repository.deleteEvent(currentEvent.getId());
            Toast.makeText(requireContext(), R.string.delete_event_success, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.delete_event_failure, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupActionButtons(@NonNull Event event, String userID) {
        // Only show buttons if registration is open
        if (event.getStatus() == Event.Status.REG_OPEN) {
            binding.buttonContainer.setVisibility(View.VISIBLE);

            // Set up join/leave button
            setupButton(event, userID);
        } else {
            // Hide entire button container for closed events
            binding.buttonContainer.setVisibility(View.GONE);
        }
    }

    private void setupButton(@NonNull Event event, String userID) {
        // Check if user is already on waitlist
        boolean isOnWaitlist = event.isOnWaitingList(userID);

        if (isOnWaitlist) {
            binding.joinEventButton.setText(R.string.leave_waiting_list);
            binding.joinEventButton.setOnClickListener(v -> {
                waitingListController.leaveWaitingList(event.getId(), userID);
                Toast.makeText(requireContext(), "Left waiting list", Toast.LENGTH_SHORT).show();
            });
        } else {
            binding.joinEventButton.setText(R.string.join_waiting_list);
            binding.joinEventButton.setOnClickListener(v -> {
                waitingListController.joinWaitingList(event.getId(), userID);
                Toast.makeText(requireContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void determineAdminStatus() {
        isAdmin = AdminGate.isAdmin(requireContext());
        configAdminButton();

        if (profileRepository == null || currentUserDeviceId == null) {
            return;
        }
        profileRepository.findUserById(currentUserDeviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                boolean repoAdmin = profile != null && profile.isAdmin();
                if (repoAdmin != isAdmin && binding != null) {
                    isAdmin = repoAdmin;
                    configAdminButton();
                }
            }

            @Override
            public void onDeleted() {
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Event event = null;
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        }
        if (event != null) {
            outState.putSerializable(ARG_EVENT, event);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}