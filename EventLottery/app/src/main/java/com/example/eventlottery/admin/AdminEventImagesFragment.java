package com.example.eventlottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.eventlottery.R;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentAdminEventImagesBinding;
import com.example.eventlottery.model.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

/**
 * An admin-only screen that displays a grid of all event posters for review and moderation.
 * This fragment fetches all events, filters for those with posters, and displays them.
 */
public class AdminEventImagesFragment extends Fragment implements AdminEventImagesAdapter.Listener {

    /** View binding for the fragment's layout. */
    private FragmentAdminEventImagesBinding binding;
    /** Repository for accessing and modifying event data. */
    private EventRepository eventRepository;
    /** Adapter for the RecyclerView that displays the grid of event posters. */
    private AdminEventImagesAdapter adapter;

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
        binding = FragmentAdminEventImagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view has been created. Initializes the repository, adapter, and RecyclerView.
     * Sets up UI listeners and observes the event list from the repository to populate the grid.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository and adapter
        eventRepository = RepositoryProvider.getEventRepository();
        adapter = new AdminEventImagesAdapter(this);

        // Configure the RecyclerView with a grid layout and the adapter
        binding.imageGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.imageGrid.setAdapter(adapter);

        // Set up the back button to navigate back
        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Observe the list of events and update the UI accordingly
        eventRepository.observeEvents().observe(getViewLifecycleOwner(), this::bindEvents);
    }

    /**
     * Processes the list of events from the repository, filters out events without a poster URL,
     * and submits the filtered list to the adapter. Manages the visibility of the empty state view.
     *
     * @param events The list of {@link Event} objects from the observer, which can be null.
     */
    private void bindEvents(@Nullable List<Event> events) {
        List<Event> safe = events != null ? events : Collections.emptyList();
        List<Event> filtered = new java.util.ArrayList<>();
        // Filter the list to include only events that have a poster URL
        for (Event event : safe) {
            if (event != null && event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                filtered.add(event);
            }
        }
        // Submit the filtered list to the adapter
        adapter.submitList(filtered);
        // Show or hide the empty view based on whether the filtered list is empty
        binding.emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Handles the request to remove an event's poster. Shows a confirmation dialog before
     * proceeding with the removal.
     *
     * @param event The non-null {@link Event} whose poster is to be removed.
     */
    @Override
    public void onRemovePoster(@NonNull Event event) {
        if (event.getId() == null) {
            return; // Cannot proceed without an event ID
        }
        if (event.getPosterUrl() == null || event.getPosterUrl().isEmpty()) {
            Toast.makeText(requireContext(), R.string.admin_event_images_no_poster, Toast.LENGTH_SHORT).show();
            return;
        }
        // Show a confirmation dialog before removing the poster
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_event_images_remove_title)
                .setMessage(getString(R.string.admin_event_images_remove_body, event.getTitle()))
                .setNegativeButton(android.R.string.cancel, null) // "Cancel" does nothing
                .setPositiveButton(R.string.admin_event_images_remove_confirm, (dialog, which) -> {
                    // On confirmation, call the repository to remove the poster
                    eventRepository.removeEventPoster(event.getId());
                    Toast.makeText(requireContext(), R.string.admin_event_images_remove_success, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * Called when the view is destroyed. Cleans up the reference to the view binding
     * to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
