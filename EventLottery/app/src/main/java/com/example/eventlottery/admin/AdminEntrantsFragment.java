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

import java.util.Collections;

/**
 * An admin-only screen that displays a list of all user profiles for moderation.
 * This fragment retrieves and observes a list of all users from the {@link ProfileRepository}
 * and displays them in a {@link androidx.recyclerview.widget.RecyclerView}.
 */
public class AdminEntrantsFragment extends Fragment implements AdminEntrantAdapter.Listener {

    /** View binding for the fragment's layout. */
    private FragmentAdminEntrantsBinding binding;
    /** Adapter for the RecyclerView that displays the list of profiles. */
    private AdminEntrantAdapter adapter;
    /** Repository for accessing and observing profile data. */
    private ProfileRepository profileRepository;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view has been created. Initializes the RecyclerView, adapter, and repository.
     * Sets up a listener for the back button and observes the list of profiles to update the UI.
     *
     * @param view The View object
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileRepository = RepositoryProvider.getProfileRepository();

        // Initialize the adapter and set it on the RecyclerView
        adapter = new AdminEntrantAdapter(this);
        binding.entrantList.setAdapter(adapter);
        binding.entrantList.setHasFixedSize(true);
        // Set up the back button to navigate to the previous screen
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
        // Observe changes in the profile data and update the adapter
        profileRepository.observeProfiles().observe(getViewLifecycleOwner(), profiles -> {
            if (profiles == null || profiles.isEmpty()) {
                // If there are no profiles, show an empty state message
                adapter.submitList(Collections.emptyList());
                binding.emptyView.setVisibility(View.VISIBLE);
            } else {
                // Otherwise, submit the list of profiles to the adapter
                adapter.submitList(profiles);
                binding.emptyView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Called when the view is destroyed. Cleans up the reference to the view binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Handles the selection of a profile from the list. Navigates to the
     * {@link AdminEditProfileFragment}, passing the selected profile's ID.
     *
     * @param profile The {@link Profile} that was selected.
     */
    @Override
    public void onProfileSelected(@NonNull Profile profile) {
        // Create a bundle to pass the profile ID to the next fragment
        Bundle args = new Bundle();
        args.putString(AdminEditProfileFragment.ARG_PROFILE_ID, profile.getDeviceID());
        // Navigate to the edit profile screen
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_adminEntrantsFragment_to_adminEditProfileFragment, args);
    }
}
