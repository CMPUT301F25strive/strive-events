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

public class AdminAllEventsFragment extends Fragment {

    private FragmentAdminAllEventsBinding binding;
    private EventListAdapter adapter;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAdminAllEventsBinding.inflate(inflater, container, false);

        // Back button: pop back stack
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // Init Firestore, adapter, RecyclerView, etc.
        firestore = FirebaseFirestore.getInstance();

        adapter = new EventListAdapter(event -> {
            // Pass the Event as an argument
            Bundle args = new Bundle();
            args.putSerializable("event", event);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminAllEventsFragment_to_eventDetailFragment, args);
        });


        binding.eventRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventRecycler.setAdapter(adapter);

        binding.eventRefresh.setOnRefreshListener(this::loadEventsOnce);
        loadEventsOnce();

        return binding.getRoot();
    }


    private void loadEventsOnce() {
        // Show loading (SwipeRefreshLayout spinner; you can also use loadingIndicator)
        binding.eventRefresh.setRefreshing(true);

        firestore.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            // keep Firestore id in model
                            event.setId(doc.getId());
                            events.add(event);
                        }
                    }
                    adapter.submitList(events);
                    binding.eventRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    binding.eventRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(),
                            "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}