package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.data.FirebaseProfileRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;
import com.example.eventlottery.model.PushNotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendNotificationFragment extends Fragment implements EntrantAdapter.Listener {

    private RecyclerView entrantRecyclerView;
    private EditText messageInput;
    private ImageButton backButton;
    private ToggleButton toggleAll, toggleChosen, toggleCancelled;

    private ProfileRepository profileRepository;
    private EntrantAdapter adapter;

    private List<Profile> allProfiles = new ArrayList<>();
    private List<Profile> displayedProfiles = new ArrayList<>();

    private Event currentEvent;
    private String localDeviceId;

    private boolean allToggled = false;
    private boolean chosenToggled = false;
    private boolean cancelledToggled = false;
    private String activeFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entrantRecyclerView = view.findViewById(R.id.entrantRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        backButton = view.findViewById(R.id.backButton);
        toggleAll = view.findViewById(R.id.toggleAll);
        toggleChosen = view.findViewById(R.id.toggleChosen);
        toggleCancelled = view.findViewById(R.id.toggleCancelled);

        profileRepository = new FirebaseProfileRepository();
        adapter = new EntrantAdapter(this);

        entrantRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        entrantRecyclerView.setAdapter(adapter);

        localDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get current event from arguments
        if (getArguments() != null) {
            currentEvent = (Event) getArguments().getSerializable("currentEvent");
        }

        if (currentEvent == null) {
            Toast.makeText(requireContext(), "No event selected", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        // Load profiles from repository
        profileRepository.observeProfiles().observe(getViewLifecycleOwner(), profiles -> {
            if (profiles == null) return;

            allProfiles.clear();
            allProfiles.addAll(profiles);

            refreshWaitingListDisplay();
        });

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        toggleAll.setOnClickListener(v -> applyToggleFilter("all"));
        toggleChosen.setOnClickListener(v -> applyToggleFilter("chosen"));
        toggleCancelled.setOnClickListener(v -> applyToggleFilter("cancelled"));

        view.findViewById(R.id.sendButton).setOnClickListener(v -> sendNotifications());
    }

    private void refreshWaitingListDisplay() {
        // Only show profiles in the waiting list, excluding self
        displayedProfiles = allProfiles.stream()
                .filter(p -> currentEvent.getWaitingList().contains(p.getDeviceID()))
                .filter(p -> !localDeviceId.equals(p.getDeviceID()))
                .collect(Collectors.toList());

        adapter.submitList(displayedProfiles);
    }

    private void applyToggleFilter(String filter) {
        if (displayedProfiles.isEmpty()) return;

        boolean toggleState;

        // Logic to reset selections if switching filters and clicking the same filter twice
        if (filter.equals("all")) {
            if (!allToggled) adapter.clearSelection();
            allToggled = !allToggled;
            toggleState = allToggled;
            chosenToggled = false;
            cancelledToggled = false;
        } else if (filter.equals("chosen")) {
            if (!chosenToggled) adapter.clearSelection();
            chosenToggled = !chosenToggled;
            toggleState = chosenToggled;
            allToggled = false;
            cancelledToggled = false;
        } else if (filter.equals("cancelled")) {
            if (!cancelledToggled) adapter.clearSelection();
            cancelledToggled = !cancelledToggled;
            toggleState = cancelledToggled;
            allToggled = false;
            chosenToggled = false;
        } else {
            return;
        }

        // Apply the toggle to get the matching profiles to the toggle only
        for (Profile p : displayedProfiles) {
            boolean isChosen = currentEvent.getInvitedList().contains(p.getDeviceID());
            boolean isCancelled = currentEvent.getCanceledList().contains(p.getDeviceID());

            boolean matchesFilter = false;
            switch (filter) {
                case "all":
                    matchesFilter = true;
                    break;
                case "chosen":
                    matchesFilter = isChosen;
                    break;
                case "cancelled":
                    matchesFilter = isCancelled;
                    break;
            }

            if (matchesFilter) {
                adapter.checkProfile(p, toggleState);
            }
        }

        // Move selected profiles to the top, keep everyone else below them
        List<Profile> selectedAtTop = new ArrayList<>();
        List<Profile> notSelected = new ArrayList<>();
        for (Profile p : displayedProfiles) {
            if (adapter.getSelectedProfiles().contains(p)) {
                selectedAtTop.add(p);
            } else {
                notSelected.add(p);
            }
        }
        selectedAtTop.addAll(notSelected);
        adapter.submitList(selectedAtTop);
    }

    private void sendNotifications() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Profile> selected = adapter.getSelectedProfiles();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Select at least one entrant", Toast.LENGTH_SHORT).show();
            return;
        }

        PushNotificationService service = new PushNotificationService(requireContext());

        for (Profile profile : selected) {
            String receiverId = profile.getDeviceID();
            if (receiverId != null && !receiverId.isEmpty()) {
                service.sendNotification(localDeviceId, receiverId, message, false);
            }
        }

        Toast.makeText(requireContext(), "Notifications sent!", Toast.LENGTH_SHORT).show();
        messageInput.setText("");
        adapter.clearSelection();
    }

    @Override
    public void onProfileSelected(@NonNull Profile profile) {
        adapter.toggleSelection(profile);
    }
}