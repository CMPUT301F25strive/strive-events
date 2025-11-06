package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

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

        // Todo: Use DeviceIdentityService to get the device ID
        String deviceId = "NA";
        viewModel.loadProfile(deviceId);

        // Observe profile state
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            // Check deletion
            if (state.isDeleted()) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                // TODO: Set navigation to welcome page.
            }

            // Normal profile display
            Profile profile = state.getProfile();
            if (profile != null) {
                binding.profileName.setText(profile.getName());
                binding.profileEmail.setText(profile.getEmail());
                binding.profilePhone.setText(profile.getPhone());
            }

            // Other potential errors
            if (state.getErrorMessage() != null) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Edit Profile button
        binding.buttonEditProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_profileEditFragment)
        );

        // Delete Profile menu
        binding.menuDeleteAccount.setOnClickListener(v ->
                // TODO: add a confirmation dialog
                viewModel.deleteProfile()
        );

        // Bottom navigation
        setupBottomNav();
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                // already here
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                // go to home (EntrantEventListFragment)
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