package com.example.eventlottery.entrant;

import android.os.Bundle;
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
import com.example.eventlottery.data.InMemProfileRepo;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentProfileBinding;
import com.example.eventlottery.model.EntrantProfile;
import com.example.eventlottery.viewmodel.EntrantProfileViewModel;
import com.example.eventlottery.viewmodel.EntrantProfileViewModelFactory;


public class EntrantProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private EntrantProfileViewModel viewModel;

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

        // Shared profile ViewModel (same as EditProfileFragment)
        ProfileRepository repo = RepositoryProvider.getProfileRepository();
        EntrantProfileViewModelFactory factory = new EntrantProfileViewModelFactory(repo);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(EntrantProfileViewModel.class);

        // Observe profile state
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            EntrantProfile profile = state.getProfile();
            if (profile != null) {
                binding.profileName.setText(profile.getName());
                binding.profileEmail.setText(profile.getEmail());
            }

            if (state.getErrorMessage() != null) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Edit Profile button
        binding.buttonEditProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.editProfileFragment)
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
                // go back to home (EntrantEventListFragment)
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.entrantEventListFragment, false);
                return true;
            } else if (item.getItemId() == R.id.nav_my_events) {
                // later: navigate to "my events" when you have it
                Toast.makeText(requireContext(), R.string.nav_my_events, Toast.LENGTH_SHORT).show();
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
