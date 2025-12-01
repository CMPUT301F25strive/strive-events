package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentWaitingListBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment for showing the waiting list of an event
 */

public class WaitingListFragment extends Fragment {

    public static final String ARG_EVENT_ID = "event_id";

    private FragmentWaitingListBinding binding;
    private WaitingListAdapter adapter;

    private EventRepository eventRepository;
    private ProfileRepository profileRepository;

    private String eventId;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWaitingListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventRepository = RepositoryProvider.getEventRepository();
        profileRepository = RepositoryProvider.getProfileRepository();

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        adapter = new WaitingListAdapter();
        binding.waitingListRecyclerView.setAdapter(adapter);
        binding.waitingListRecyclerView.setHasFixedSize(true);

        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        observeEvent();
    }

    /**
     * Observe the event and load the profiles
     */
    private void observeEvent() {
        if (eventId == null) {
            showMessage("No users in the waiting list");
            return;
        }

        eventRepository.observeEvents().observe(getViewLifecycleOwner(), events -> {
            if (events == null || events.isEmpty()) {
                showMessage("No users in the waiting list");
                return;
            }

            Event targetEvent = null;
            for (Event e : events) {
                if (eventId.equals(e.getId())) {
                    targetEvent = e;
                    break;
                }
            }

            if (targetEvent == null || targetEvent.getWaitingList() == null || targetEvent.getWaitingList().isEmpty()) {
                showMessage("No users in the waiting list");
                return;
            }

            loadProfiles(targetEvent.getWaitingList());
        });
    }

    /**
     * Load profiles from the repository
     * @param userIds
     */
    private void loadProfiles(List<String> userIds) {
        if (userIds.isEmpty()) {
            showMessage("No users in the waiting list");
            return;
        }

        List<Profile> profiles = new ArrayList<>();
        final int total = userIds.size();
        final int[] loadedCount = {0};

        for (String uid : userIds) {
            profileRepository.findUserById(uid, new ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(Profile profile) {
                    if (profile != null) profiles.add(profile);
                    incrementAndCheck();
                }

                @Override
                public void onDeleted() {
                    incrementAndCheck();
                }

                @Override
                public void onError(String message) {
                    Log.w("WaitingListFragment", "Error loading profile: " + message);
                    incrementAndCheck();
                }

                private void incrementAndCheck() {
                    loadedCount[0]++;
                    if (loadedCount[0] == total) {
                        requireActivity().runOnUiThread(() -> {
                            Collections.sort(profiles, Comparator.comparing(p -> p.getName() != null ? p.getName() : ""));
                            adapter.submitList(new ArrayList<>(profiles));

                            if (profiles.isEmpty()) {
                                showMessage("No users in the waiting list");
                            } else {
                                binding.errorMessage.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Show a message to the user
     *
     * @param message
     */
    private void showMessage(@NonNull String message) {
        binding.errorMessage.setText(message);
        binding.errorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Called when the fragment is no longer in use.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}