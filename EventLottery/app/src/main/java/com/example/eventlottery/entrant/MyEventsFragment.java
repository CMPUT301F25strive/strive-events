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
import com.example.eventlottery.databinding.FragmentMyEventsBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.viewmodel.MyEventsViewModel;
import com.example.eventlottery.viewmodel.MyEventsViewModelFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows events the entrant has joined or is on the waiting list for.
 */
public class MyEventsFragment extends Fragment implements EventListAdapter.Listener {

    private FragmentMyEventsBinding binding;
    private MyEventsViewModel viewModel;
    private EventListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up UI
        setupRecycler();
        setupBottomNav();
        setupToggleGroup();
        binding.pageHeader.setText(R.string.my_events_page_header);

        // Initialize ViewModel with context for device ID
        viewModel = new ViewModelProvider(this, new MyEventsViewModelFactory(requireContext()))
                .get(MyEventsViewModel.class);

        // Pull-to-refresh
        binding.eventRefresh.setOnRefreshListener(() -> viewModel.refresh());

        // Get current state
        viewModel.getCurrentSegment().observe(getViewLifecycleOwner(), segment -> {
            updateToggleButtons(segment);
            updateSectionHeader();
        });
        viewModel.getState().observe(getViewLifecycleOwner(), this::renderState);
    }

    private void setupRecycler() {
        adapter = new EventListAdapter(this);
        binding.eventRecycler.setAdapter(adapter);
        binding.eventRecycler.setHasFixedSize(true);
    }

    private void setupToggleGroup() {
        // Set initial state
        binding.menuWaitingList.setChecked(true); //

        // Initialize listeners
        binding.myEventsToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.menuWaitingList) {
                    viewModel.switchSegment(MyEventsViewModel.EventSegment.WAITING_LIST);
                } else if (checkedId == R.id.menuAccepted) {
                    viewModel.switchSegment(MyEventsViewModel.EventSegment.ACCEPTED);
                } else if (checkedId == R.id.menuHistory) {
                    viewModel.switchSegment(MyEventsViewModel.EventSegment.HISTORY);
                }
            }
        });
    }

    private void updateToggleButtons(MyEventsViewModel.EventSegment segment) {
        binding.menuWaitingList.setChecked(segment == MyEventsViewModel.EventSegment.WAITING_LIST);
        binding.menuAccepted.setChecked(segment == MyEventsViewModel.EventSegment.ACCEPTED);
        binding.menuHistory.setChecked(segment == MyEventsViewModel.EventSegment.HISTORY);
    }

    private void updateSectionHeader() {
        MyEventsViewModel.EventSegment segment = viewModel.getCurrentSegment().getValue();
        if (segment != null) {
            switch (segment) {
                case WAITING_LIST:
                    binding.upcomingHeader.setText("My Waiting List Events");
                    break;
                case ACCEPTED:
                    binding.upcomingHeader.setText("My Upcoming Events");
                    break;
                case HISTORY:
                    binding.upcomingHeader.setText("My Event History");
                    break;
            }
        }
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_my_events);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (item.getItemId() == R.id.nav_home) {
            NavHostFragment.findNavController(this)
                    .popBackStack(R.id.entrantEventListFragment, false);
            return true;
            } else if (id == R.id.nav_profile) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_myEventsFragment_to_profileFragment);
                return true;
            } else return id == R.id.nav_my_events;
        });
    }

    private void renderState(@NonNull EventListUiState state) {
        binding.loadingIndicator.setVisibility(state.loading ? View.VISIBLE : View.GONE);
        binding.eventRefresh.setRefreshing(false);

        List<Event> myEvents = state.events != null ? state.events : new ArrayList<>();
        adapter.submitList(myEvents);

        if (state.errorMessage != null) {
            Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show();
        } else if (myEvents.isEmpty()) {
            showEmptyStateMessage();
        } else {
            binding.errorMessage.setVisibility(View.GONE);
        }
    }

    private void showEmptyStateMessage() {
        MyEventsViewModel.EventSegment segment = viewModel.getCurrentSegment().getValue();
        String message = "";

        if (segment != null) {
            switch (segment) {
                case WAITING_LIST:
                    message = "You haven't joined any waiting lists yet";
                    break;
                case ACCEPTED:
                    message = "No upcoming events";
                    break;
                case HISTORY:
                    message = "No past events attending";
                    break;
            }
        }

        binding.errorMessage.setText(message);
        binding.errorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onEventSelected(@NonNull Event event) {
        Bundle args = new Bundle();
        args.putSerializable(EventDetailFragment.ARG_EVENT, event);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_myEventsFragment_to_eventDetailFragment, args);
    }
}