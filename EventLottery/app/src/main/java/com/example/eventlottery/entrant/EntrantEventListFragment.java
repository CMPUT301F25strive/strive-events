package com.example.eventlottery.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.FragmentEventListBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.EventFilter;
import com.example.eventlottery.model.QRScanner;
import com.example.eventlottery.organizer.OrganizerGate;
import com.example.eventlottery.viewmodel.EntrantEventListViewModel;
import com.example.eventlottery.viewmodel.EntrantEventListViewModelFactory;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

/**
 * entrant home feed showing joinable events.
 */
public class EntrantEventListFragment extends Fragment implements EventListAdapter.Listener, EventFilterFragment.Listener {

    private FragmentEventListBinding binding;
    private EntrantEventListViewModel viewModel;
    private EventListAdapter adapter;
    private EventFilter filter = new EventFilter();
    private final List<Event> allEvents = new ArrayList<>();
    private QRScanner qrScanner;

    @Override
    public void onResume() {
        super.onResume();
        binding.bottomNavigation.post(() -> {
            binding.bottomNavigation.setSelectedItemId(-1); // deselect first
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        });
        updateOrganizerActions();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();
        setupBottomNav();
        qrScanner = new QRScanner();
//        binding.pageHeader.setText(R.string.home_page_header);
        updateOrganizerActions();

        viewModel = new ViewModelProvider(this, new EntrantEventListViewModelFactory())
                .get(EntrantEventListViewModel.class);

        binding.eventRefresh.setOnRefreshListener(() -> {
            binding.errorMessage.setVisibility(View.GONE);
            viewModel.refresh();
        });

        viewModel.getState().observe(getViewLifecycleOwner(), this::renderState);

        binding.filterButton.setOnClickListener(v -> {
            // Make filter fragment a child fragment of this fragment.
            EventFilterFragment.newInstance(filter).show(getChildFragmentManager(), "filter");
        });

        // === ADD NAVIGATION TO CREATE EVENT ===
        binding.fabAddEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_entrantEventListFragment_to_createEventFragment)
        );

        // QR code scanner
        binding.qrShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanner.startScanner(EntrantEventListFragment.this);
            }
        });
    }

    private void updateOrganizerActions() {
        if (binding == null) return;
        boolean canOrganize = OrganizerGate.hasOrganizerAccess(requireContext());
        binding.fabAddEvent.setVisibility(canOrganize ? View.VISIBLE : View.GONE);
    }
    private void setupRecycler() {
        adapter = new EventListAdapter(this);
        binding.eventRecycler.setAdapter(adapter);
        binding.eventRecycler.setHasFixedSize(true);
    }
    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
         if (item.getItemId() == R.id.nav_profile) {
             NavHostFragment.findNavController(this)
                     .navigate(R.id.action_entrantEventListFragment_to_profileFragment);
             return true;
         }else if (item.getItemId() == R.id.nav_home) {
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.entrantEventListFragment, false);
                return true;
            } else if (item.getItemId() == R.id.nav_my_events) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.myEventsFragment);
                return true;
            }
            return false;
        });
    }
    private void renderState(@NonNull EventListUiState state) {
        binding.loadingIndicator.setVisibility(state.loading ? View.VISIBLE : View.GONE);
        binding.eventRefresh.setRefreshing(false);

        allEvents.clear();

        long now = System.currentTimeMillis();

        // TODO: What do we display, open events only,
        //  or + temporarily closed events (full),
        //  or + future events (will open) as well?
        // Now include all the above situations to display
        for (Event e : state.events) {
            if (e.isRegOpen() || e.isRegClosed()) {
                allEvents.add(e);
            }
        }

        applyFilter();

        if (state.errorMessage != null) {
            showMessage(state.errorMessage);
        } else if (allEvents.isEmpty()) {
            showMessage(getString(R.string.no_events_placeholder));
        } else {
            binding.errorMessage.setVisibility(View.GONE);
        }
    }

    private void showMessage(@NonNull String message) {
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
                .navigate(R.id.action_entrantEventListFragment_to_eventDetailFragment, args);
    }

    @Override
    public void onFilterChanged(@NonNull EventFilter newFilter) {
        this.filter = newFilter;
        applyFilter();  // Re-filter as the filter changes
    }

    private void applyFilter() {
        // Fetch all events and pass them one by one for filtering
        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            if (filter == null || filter.match(e)) {
                filtered.add(e);
            }
        }
        // Show empty view if filtered list is empty
        if (filtered.isEmpty()) {
            binding.errorMessage.setText(getString(R.string.no_events_placeholder));
            binding.errorMessage.setVisibility(View.VISIBLE);
        } else {
            binding.errorMessage.setVisibility(View.GONE);
        }
        // Submit filtered list to adapter
        adapter.submitList(filtered);
    }


    //QR Code activity after a scan
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result == null) return;

        if (result.getContents() == null) {
            Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        String scanned = result.getContents();
        Event event = qrScanner.extractEvent(scanned);

        if (event == null || event.getId() == null) {
            Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to the specific event's details
        NavDirections action =
                EntrantEventListFragmentDirections.actionEntrantEventListFragmentToEventDetailFragmentSpecific(event);
        Navigation.findNavController(requireView()).navigate(action);
    }


}
