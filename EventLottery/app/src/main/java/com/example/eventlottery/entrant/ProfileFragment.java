package com.example.eventlottery.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentProfileBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;
import com.example.eventlottery.model.QRScanner;
import com.example.eventlottery.organizer.OrganizerAccessCache;
import com.example.eventlottery.viewmodel.ProfileViewModel;
import com.example.eventlottery.viewmodel.ProfileViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private Switch notificationSwitch;
    private QRScanner qrScanner;
    private String deviceId;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProfileRepository repo = RepositoryProvider.getProfileRepository();
        ProfileViewModelFactory factory = new ProfileViewModelFactory(repo);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(ProfileViewModel.class);
        qrScanner = new QRScanner();

        // Get device ID
        deviceId = Secure.getString(requireContext().getContentResolver(), Secure.ANDROID_ID);
        Log.d("Device ID", "Direct get(): " + deviceId);

        viewModel.loadProfile(deviceId);

        // Observe profile state
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.isDeleted()) {
                OrganizerAccessCache.clear(deviceId);
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_welcomeFragment);
                return;
            }

            Profile profile = state.getProfile();
            if (profile != null) {
                binding.profileName.setText(profile.getName());
                binding.profileEmail.setText(profile.getEmail());
                binding.profilePhone.setText(profile.getPhone());
                binding.menuAdminEntrants.setVisibility(profile.isAdmin() ? View.VISIBLE : View.GONE);
                binding.menuAdminEvents.setVisibility(profile.isAdmin() ? View.VISIBLE : View.GONE);
                binding.menuAdminNotifications.setVisibility(profile.isAdmin() ? View.VISIBLE : View.GONE);
                binding.menuAdminImages.setVisibility(profile.isAdmin() ? View.VISIBLE : View.GONE);
                binding.adminBadge.setVisibility(profile.isAdmin() ? View.VISIBLE : View.GONE);
                notificationSwitch.setChecked(profile.getNotificationSettings());
                OrganizerAccessCache.setAllowed(profile.getDeviceID(), profile.isOrganizer());
            } else {
                binding.menuAdminEntrants.setVisibility(View.GONE);
                binding.menuAdminEvents.setVisibility(View.GONE);
                binding.menuAdminNotifications.setVisibility(View.GONE);
                binding.menuAdminImages.setVisibility(View.GONE);
                binding.adminBadge.setVisibility(View.GONE);
            }

            if (state.getErrorMessage() != null && !state.isDeleted()) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Admin Entrants
        binding.menuAdminEntrants.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_adminEntrantsFragment)
        );

        binding.menuAdminEvents.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_adminAllEventsFragment)
        );

        binding.menuAdminNotifications.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_adminNotificationsFragment)
        );

        binding.menuAdminImages.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_adminEventImagesFragment)
        );

        // Edit Profile
        binding.buttonEditProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_profileEditFragment)
        );

        // === Delete Account — Material3 Styled Dialog (matches Event delete dialog) ===

        binding.menuDeleteAccount.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteProfile())
                    .show();
        });

        // Notification toggle
        notificationSwitch = binding.notificationSwitch;
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(requireContext(),
                    "Notifications " + (isChecked ? "ON" : "OFF"),
                    Toast.LENGTH_SHORT).show();
            viewModel.updateNotifications(isChecked);
        });

        // Guidelines page
        binding.menuGuidelines.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_profileGuidelinesFragment)
        );

        // QR code scanner
        binding.qrShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanner.startScanner(ProfileFragment.this);
            }
        });

        setupBottomNav();
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.entrantEventListFragment, false);
                return true;
            } else if (item.getItemId() == R.id.nav_my_events) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.myEventsFragment);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //QR Code activity after a scan
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result == null) return;

        if (result.getContents() == null) {
            Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        String scanned = result.getContents();
        Event event = qrScanner.extractEvent(scanned);

        if (event == null || event.getId() == null) {
            Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to the specific event's details
        NavDirections action =
                ProfileFragmentDirections.actionProfileFragmentToEventDetailFragment(event);
        Navigation.findNavController(requireView()).navigate(action);
    }


}
