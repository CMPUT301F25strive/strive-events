package com.example.eventlottery.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.FragmentMyEventsBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.QRScanner;
import com.example.eventlottery.organizer.OrganizerGate;
import com.example.eventlottery.viewmodel.MyEventsViewModel;
import com.example.eventlottery.viewmodel.MyEventsViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyEventsFragment extends Fragment implements EventListAdapter.Listener {

    private FragmentMyEventsBinding binding;
    private MyEventsViewModel viewModel;
    private EventListAdapter adapter;
    private QRScanner qrScanner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecycler();
        setupBottomNav();
        setupToggleGroup();
        qrScanner = new QRScanner();
        updateOrganizerActions();

        viewModel = new ViewModelProvider(this,
                new MyEventsViewModelFactory(requireContext()))
                .get(MyEventsViewModel.class);

        binding.eventRefresh.setOnRefreshListener(() -> viewModel.refresh());

        viewModel.getCurrentSegment().observe(getViewLifecycleOwner(),
                segment -> updateToggleButtons(segment));

        viewModel.getState().observe(getViewLifecycleOwner(), this::renderState);

        binding.fabAddEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_myEventsFragment_to_createEventFragment));

        // QR code scanner
        binding.qrShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScanner.startScanner(MyEventsFragment.this);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateOrganizerActions();
    }

    private void setupRecycler() {
        adapter = new EventListAdapter(this);
        binding.eventRecycler.setAdapter(adapter);
        binding.eventRecycler.setHasFixedSize(true);
    }

    private void setupToggleGroup() {
        List<MaterialButton> buttons = Arrays.asList(
                binding.menuWaitingList,
                binding.menuAccepted,
                binding.menuHistory,
                binding.menuHosted
        );

        int selectedColor = ContextCompat.getColor(requireContext(), R.color.strive_segment_selected);
        int unselectedColor = ContextCompat.getColor(requireContext(), R.color.strive_segment_unselected);

        binding.menuWaitingList.setBackgroundColor(selectedColor);

        binding.myEventsToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            for (MaterialButton button : buttons) {
                button.setBackgroundColor(button.getId() == checkedId ? selectedColor : unselectedColor);
            }

            if (checkedId == R.id.menuWaitingList) {
                viewModel.switchSegment(MyEventsViewModel.EventSegment.WAITING_LIST);
            } else if (checkedId == R.id.menuAccepted) {
                viewModel.switchSegment(MyEventsViewModel.EventSegment.ACCEPTED);
            } else if (checkedId == R.id.menuHistory) {
                viewModel.switchSegment(MyEventsViewModel.EventSegment.HISTORY);
            } else if (checkedId == R.id.menuHosted) {
                viewModel.switchSegment(MyEventsViewModel.EventSegment.HOSTED);
            }
        });
    }

    private void updateToggleButtons(MyEventsViewModel.EventSegment segment) {
        List<MaterialButton> buttons = Arrays.asList(
                binding.menuWaitingList,
                binding.menuAccepted,
                binding.menuHistory,
                binding.menuHosted
        );

        int selectedColor = ContextCompat.getColor(requireContext(), R.color.strive_segment_selected);
        int unselectedColor = ContextCompat.getColor(requireContext(), R.color.strive_segment_unselected);

        for (MaterialButton btn : buttons) {
            boolean isSelected =
                    (segment == MyEventsViewModel.EventSegment.WAITING_LIST && btn.getId() == R.id.menuWaitingList) ||
                            (segment == MyEventsViewModel.EventSegment.ACCEPTED     && btn.getId() == R.id.menuAccepted) ||
                            (segment == MyEventsViewModel.EventSegment.HISTORY      && btn.getId() == R.id.menuHistory) ||
                            (segment == MyEventsViewModel.EventSegment.HOSTED       && btn.getId() == R.id.menuHosted);

            btn.setBackgroundColor(isSelected ? selectedColor : unselectedColor);
        }
    }

    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_my_events);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.entrantEventListFragment, false);
                return true;
            }

            if (id == R.id.nav_profile) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_myEventsFragment_to_profileFragment);
                return true;
            }

            return id == R.id.nav_my_events;
        });
    }

    private void renderState(@NonNull EventListUiState state) {
        binding.loadingIndicator.setVisibility(state.loading ? View.VISIBLE : View.GONE);
        binding.eventRefresh.setRefreshing(false);

        List<Event> events = state.events != null ? state.events : new ArrayList<>();
        adapter.submitList(events);

        if (state.errorMessage != null) {
            Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show();
        }

        if (events.isEmpty()) {
            showEmptyStateMessage();
        } else {
            binding.errorMessage.setVisibility(View.GONE);
        }
    }

    private void showEmptyStateMessage() {
        MyEventsViewModel.EventSegment segment = viewModel.getCurrentSegment().getValue();
        String message = "";

        if (segment == MyEventsViewModel.EventSegment.WAITING_LIST)
            message = "You haven't joined any waiting lists";
        else if (segment == MyEventsViewModel.EventSegment.ACCEPTED)
            message = "No upcoming events";
        else if (segment == MyEventsViewModel.EventSegment.HISTORY)
            message = "No past events";
        else if (segment == MyEventsViewModel.EventSegment.HOSTED)
            message = "You haven't hosted any events";

        binding.errorMessage.setText(message);
        binding.errorMessage.setVisibility(View.VISIBLE);
    }

    private void updateOrganizerActions() {
        if (binding == null) return;
        boolean canOrganize = OrganizerGate.hasOrganizerAccess(requireContext());
        binding.fabAddEvent.setVisibility(canOrganize ? View.VISIBLE : View.GONE);
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
                MyEventsFragmentDirections.actionMyEventsFragmentToEventDetailFragmentSpecific(event);
        Navigation.findNavController(requireView()).navigate(action);
    }
}
