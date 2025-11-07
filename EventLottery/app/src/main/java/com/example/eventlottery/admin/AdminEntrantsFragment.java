package com.example.eventlottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentAdminEntrantsBinding;
import com.example.eventlottery.model.Profile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Admin-only screen that lists all entrant profiles for moderation.
 */
public class AdminEntrantsFragment extends Fragment implements AdminEntrantAdapter.Listener {

    private FragmentAdminEntrantsBinding binding;
    private AdminEntrantAdapter adapter;
    private ProfileRepository profileRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileRepository = RepositoryProvider.getProfileRepository();

        adapter = new AdminEntrantAdapter(this);
        binding.entrantList.setAdapter(adapter);
        binding.entrantList.setHasFixedSize(true);

        binding.adminEntrantsToolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        profileRepository.observeProfiles().observe(getViewLifecycleOwner(), profiles -> {
            List<Profile> entrants = filterEntrants(profiles);
            adapter.submitList(entrants);
            binding.emptyView.setVisibility(entrants.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onProfileSelected(@NonNull Profile profile) {
        Bundle args = new Bundle();
        args.putString(AdminEditProfileFragment.ARG_PROFILE_ID, profile.getDeviceID());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_adminEntrantsFragment_to_adminEditProfileFragment, args);
    }

    // Only show entrant accounts; admins/organizers will live on their own tabs.
    private List<Profile> filterEntrants(@Nullable List<Profile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Profile> entrantsOnly = new ArrayList<>();
        for (Profile profile : profiles) {
            if (profile != null && profile.getRole() == Profile.Role.USER) {
                entrantsOnly.add(profile);
            }
        }
        return entrantsOnly;
    }
}
