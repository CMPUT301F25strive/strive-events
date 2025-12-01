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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.FragmentAdminAllEventsBinding;
import com.example.eventlottery.entrant.EventListAdapter;
import com.example.eventlottery.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that allows an admin user to view all events in the system.
 */
public class AdminAllEventsFragment extends Fragment {

    // View binding for fragment_admin_all_events.xml
    private FragmentAdminAllEventsBinding binding;

    // Adapter used to display the list of events for the admin.
    private EventListAdapter adapter;

    // Firestore instance used to fetch events.
    private FirebaseFirestore firestore;

    /**
     * Inflates the layout, initializes UI components, and triggers
     * the initial load of all events from Firestore.
     *
     * @param inflater  LayoutInflater used to inflate the layout.
     * @param container Optional parent view that this fragment's UI should be attached to.
     * @param savedInstanceState Previous saved state, if any.
     * @return The root view of the fragment layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate view binding for this fragment
        binding = FragmentAdminAllEventsBinding.inflate(inflater, container, false);

        // Handle top-bar back button: navigate up in the NavController stack
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Initialize adapter with click listener that navigates to event detail
        adapter = new EventListAdapter(event -> {
            // Bundle the selected Event, so EventDetailFragment can display its details
            Bundle args = new Bundle();
            args.putSerializable("event", event);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminAllEventsFragment_to_eventDetailFragment, args);
        });

        // Setup RecyclerView with a vertical LinearLayoutManager and the adapter
        binding.eventRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventRecycler.setAdapter(adapter);

        // Enable pull-to-refresh to reload the list of events
        binding.eventRefresh.setOnRefreshListener(this::loadEventsOnce);

        // Initial data load when the view is created
        loadEventsOnce();

        return binding.getRoot();
    }

    /**
     * Fetches all events from the Firestore "events" collection once,
     * converts them to Event objects, and displays them in the adapter.
     */
    private void loadEventsOnce() {
        // Show pull-to-refresh spinner while loading
        binding.eventRefresh.setRefreshing(true);

        firestore.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();

                    // Convert each Firestore document into an Event object
                    for (DocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            // Preserve Firestore document ID in the Event model (if used elsewhere)
                            event.setId(doc.getId());
                            events.add(event);
                        }
                    }

                    // Update the adapter with the new list of events
                    adapter.submitList(events);

                    // Hide the refresh spinner
                    binding.eventRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    // Hide the refresh spinner even if loading fails
                    binding.eventRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(),
                            "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}