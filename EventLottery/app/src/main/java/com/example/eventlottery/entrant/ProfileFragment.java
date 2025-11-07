package com.example.eventlottery.entrant;

import android.app.AlertDialog;
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
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentProfileBinding;
import com.example.eventlottery.model.Profile;
import com.example.eventlottery.viewmodel.ProfileViewModel;
import com.example.eventlottery.viewmodel.ProfileViewModelFactory;

/**
 * This is the Java code for the activity of Profile page.
 */

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private Switch notificationSwitch; // new switch reference

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

        // Get device ID
        String deviceID = Secure.getString(requireContext().getContentResolver(), Secure.ANDROID_ID);
        Log.d("Device ID", "Direct get(): " + deviceID);

        viewModel.loadProfile(deviceID);

        // Observe profile state
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            // Check deletion
            if (state.isDeleted()) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_welcomeFragment);
                return;
            }

            // Normal profile display
            Profile profile = state.getProfile();
            if (profile != null) {
                binding.profileName.setText(profile.getName());
                binding.profileEmail.setText(profile.getEmail());
                binding.profilePhone.setText(profile.getPhone());
                binding.menuAdminEntrants.setVisibility(profile.isAdmin() ? View.VISIBLE : View.GONE);
                binding.adminBadge.setVisibility(profile.isAdmin() ? View.VISIBLE : View.GONE);
            } else {
                binding.menuAdminEntrants.setVisibility(View.GONE);
                binding.adminBadge.setVisibility(View.GONE);
            }

            // Other potential errors
            if (state.getErrorMessage() != null && !state.isDeleted()) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.menuAdminEntrants.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_adminEntrantsFragment)
        );

        // Edit Profile button
        binding.buttonEditProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_profileEditFragment)
        );

        // Delete Profile menu with confirmation dialog
        binding.menuDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteProfile())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Notification switch (just UI toggle)
        notificationSwitch = binding.notificationSwitch; // connect switch from XML
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(),
                    "Notifications " + (isChecked ? "ON" : "OFF"),
                    Toast.LENGTH_SHORT).show();
            // Currently no functional behavior; just toggle UI state
        });

        binding.menuGuidelines.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_profileGuidelinesFragment)
        );

        // Guidance page
        //binding.menuGuidelines.setOnClickListener(v ->
                //
           //      );

        // Bottom navigation
        setupBottomNav();
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                return true; // already here
            } else if (item.getItemId() == R.id.nav_home) {
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.entrantEventListFragment, false);
                return true;
            } else if (item.getItemId() == R.id.nav_my_events) {
                // TODO: set a navigation to my_events page
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
}
