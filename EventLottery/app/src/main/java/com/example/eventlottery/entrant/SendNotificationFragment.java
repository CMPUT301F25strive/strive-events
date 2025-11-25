package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlottery.R;
import com.example.eventlottery.data.FirebaseProfileRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.Profile;
import com.example.eventlottery.model.PushNotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendNotificationFragment extends Fragment implements EntrantAdapter.Listener {

    private RecyclerView entrantsRecyclerView;
    private EditText messageInput;
    private Button backButton, sendNotificationButton;

    private ProfileRepository profileRepository;
    private EntrantAdapter adapter;
    private List<Profile> entrantsList = new ArrayList<>();

    private String localDeviceId;

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

        entrantsRecyclerView = view.findViewById(R.id.entrantsRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        backButton = view.findViewById(R.id.backButton);
        sendNotificationButton = view.findViewById(R.id.sendNotificationButton);

        profileRepository = new FirebaseProfileRepository();
        adapter = new EntrantAdapter(this);

        entrantsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        entrantsRecyclerView.setAdapter(adapter);

        // Current device ID
        localDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Load all profiles and exclude current device since can't send to own device
        profileRepository.observeProfiles().observe(getViewLifecycleOwner(), profiles -> {
            if (profiles != null) {
                entrantsList.clear();
                entrantsList.addAll(profiles.stream()
                        .filter(p -> !localDeviceId.equals(p.getDeviceID()))
                        .collect(Collectors.toList()));
                adapter.submitList(new ArrayList<>(entrantsList));
            }
        });

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        sendNotificationButton.setOnClickListener(v -> sendNotifications());
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
                service.sendNotification(localDeviceId, receiverId, message);
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