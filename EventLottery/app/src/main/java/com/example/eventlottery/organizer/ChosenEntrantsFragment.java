package com.example.eventlottery.organizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventlottery.R;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentChosenEntrantsBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Read-only list of invited entrants (US 02.06.01).
 */
public class ChosenEntrantsFragment extends Fragment {

    public static final String ARG_EVENT_ID = "chosen_event_id";
    public static final String ARG_EVENT_TITLE = "chosen_event_title";
    public static final String ARG_CAN_MANAGE = "chosen_can_manage";

    private FragmentChosenEntrantsBinding binding;
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;
    private ChosenEntrantAdapter adapter;
    private final List<ChosenEntrantAdapter.Row> fullRows = new ArrayList<>();
    private FilterMode currentFilter = FilterMode.ALL;

    private String eventId;
    private String initialTitle;
    private Event currentEvent;
    private boolean canManage;

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChosenEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after OnCreateView() to perform actions
     * @param view The View returned by OnCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventRepository = RepositoryProvider.getEventRepository();
        profileRepository = RepositoryProvider.getProfileRepository();

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            initialTitle = getArguments().getString(ARG_EVENT_TITLE);
            canManage = getArguments().getBoolean(ARG_CAN_MANAGE, false);
        }

        adapter = new ChosenEntrantAdapter(canManage ? this::confirmCancelEntrant : null);
        binding.chosenEntrantsRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.chosenEntrantsRecycler.setAdapter(adapter);
        binding.chosenEntrantsRecycler.setHasFixedSize(true);

        binding.filterToggleGroup.check(binding.filterAllButton.getId());
        binding.filterToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            FilterMode newMode = checkedId == binding.filterCancelledButton.getId()
                    ? FilterMode.CANCELLED
                    : FilterMode.ALL;
            if (newMode != currentFilter) {
                currentFilter = newMode;
                applyFilter();
            }
        });

        if (!TextUtils.isEmpty(initialTitle)) {
            binding.summaryEventTitle.setText(initialTitle);
        }

        binding.swipeRefresh.setOnRefreshListener(() -> {
            eventRepository.refresh();
            if (currentEvent != null) {
                loadChosenEntrants(currentEvent);
            } else {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        observeEvent();
    }

    /**
     * Find the event with its id and update the UI, show the summary of the given event.
     */
    private void observeEvent() {
        if (TextUtils.isEmpty(eventId)) {
            showEmptyState(true);
            binding.summaryEventStatus.setText(R.string.unknown);
            binding.loadingIndicator.setVisibility(View.GONE);
            return;
        }

        eventRepository.observeEvents().observe(getViewLifecycleOwner(), events -> {
            Event event = eventRepository.findEventById(eventId);
            if (event == null) {
                showEmptyState(true);
                binding.summaryEventStatus.setText(R.string.unknown);
                return;
            }

            currentEvent = event;
            bindSummary(event);
            loadChosenEntrants(event);
        });
    }

    /**
     * This function binds the summary of the given event.
     * @param event The event to bind the summary of.
     */
    private void bindSummary(@NonNull Event event) {
        if (binding == null) return;
        binding.summaryEventTitle.setText(event.getTitle());
        binding.summaryEventStatus.setText(getDisplayStatus(event));

        SummaryCounts counts = buildSummaryCounts(event);
        binding.invitedCountValue.setText(String.format(Locale.getDefault(), "%d", counts.invited));
        binding.acceptedCountValue.setText(String.format(Locale.getDefault(), "%d", counts.accepted));
        binding.pendingCountValue.setText(String.format(Locale.getDefault(), "%d", counts.pending));
        updateFilterSummary(counts);
    }

    /**
     * Load the invited entrants of the given event.
     * @param event the event to load the invited entrants of.
     */
    private void loadChosenEntrants(@NonNull Event event) {
        if (binding == null) return;
        List<String> invitedRaw = event.getInvitedList();
        LinkedHashSet<String> invitedUnique = new LinkedHashSet<>();
        if (invitedRaw != null) {
            for (String id : invitedRaw) {
                if (!TextUtils.isEmpty(id)) {
                    invitedUnique.add(id);
                }
            }
        }

        if (invitedUnique.isEmpty()) {
            fullRows.clear();
            adapter.submitList(Collections.emptyList());
            showLoading(false);
            binding.swipeRefresh.setRefreshing(false);
            showEmptyState(true);
            return;
        }

        showEmptyState(false);
        showLoading(true);

        List<ChosenEntrantAdapter.Row> rows = new ArrayList<>();
        AtomicInteger loadedCount = new AtomicInteger(0);
        int total = invitedUnique.size();

        Set<String> acceptedSet = new HashSet<>(event.getAttendeesList() != null ? event.getAttendeesList() : Collections.emptyList());
        Set<String> cancelledSet = new HashSet<>(event.getCanceledList() != null ? event.getCanceledList() : Collections.emptyList());

        for (String deviceId : invitedUnique) {
            profileRepository.findUserById(deviceId, new ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(Profile profile) {
                    deliverRow(buildRow(deviceId, profile, acceptedSet, cancelledSet));
                }

                @Override
                public void onDeleted() {
                    deliverRow(buildRow(deviceId, null, acceptedSet, cancelledSet));
                }

                @Override
                public void onError(String message) {
                    deliverRow(buildRow(deviceId, null, acceptedSet, cancelledSet));
                }

                private void deliverRow(ChosenEntrantAdapter.Row row) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        rows.add(row);
                        if (loadedCount.incrementAndGet() == total) {
                            renderRows(rows);
                        }
                    });
                }
            });
        }
    }

    /**
     * Render the rows of invited entrants with the given list.
     * @param rows the list of invited entrants
     */
    private void renderRows(@NonNull List<ChosenEntrantAdapter.Row> rows) {
        if (binding == null) return;
        Collections.sort(rows, (left, right) -> {
            String leftName = left.displayName != null ? left.displayName : "";
            String rightName = right.displayName != null ? right.displayName : "";
            return leftName.compareToIgnoreCase(rightName);
        });

        fullRows.clear();
        fullRows.addAll(rows);
        showLoading(false);
        binding.swipeRefresh.setRefreshing(false);
        applyFilter();
    }

    /**
     * Confirm the cancellation of the given entrant.
     * @param row the entrant that is to be cancelled
     */
    private void confirmCancelEntrant(@NonNull ChosenEntrantAdapter.Row row) {
        if (!canManage || currentEvent == null || row.status != ChosenEntrantAdapter.Row.Status.PENDING) {
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.chosen_cancel_confirm_title)
                .setMessage(getString(R.string.chosen_cancel_confirm_body,
                        row.displayName != null ? row.displayName : row.deviceId))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.chosen_cancel_confirm_button,
                        (dialog, which) -> performCancel(row.deviceId))
                .show();
    }

    /**
     * This is the action to perform the cancellation of the given entrant.
     * @param deviceId the id of the entrant to be cancelled
     */
    private void performCancel(@NonNull String deviceId) {
        if (currentEvent == null || TextUtils.isEmpty(deviceId) || binding == null) return;

        List<String> canceledList = new ArrayList<>(currentEvent.getCanceledList());
        if (canceledList.contains(deviceId)) {
            return;
        }

        canceledList.add(deviceId);
        currentEvent.setCanceledList(canceledList);
        eventRepository.updateCanceledList(currentEvent.getId(), canceledList);
        Snackbar.make(binding.getRoot(), R.string.chosen_cancel_success, Snackbar.LENGTH_SHORT).show();
        loadChosenEntrants(currentEvent);
    }

    /**
     * Set the visibility of the loading indicator.
     * @param show the visibility of the loading indicator
     */
    private void showLoading(boolean show) {
        if (binding == null) return;
        binding.loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Set the visibility of the empty state.
     * @param show the visibility of the empty state
     */
    private void showEmptyState(boolean show) {
        if (binding == null) return;
        binding.emptyStateGroup.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Build the row of invited entrants with the given parameters.
     * @param deviceId the id of the entrant
     * @param profile the profile of the entrant
     * @param acceptedSet accpeted list of invited entrants
     * @param cancelledSet cancelled list of invited entrants
     * @return the row of invited entrants
     */
    private ChosenEntrantAdapter.Row buildRow(@NonNull String deviceId,
                                              @Nullable Profile profile,
                                              @NonNull Set<String> acceptedSet,
                                              @NonNull Set<String> cancelledSet) {
        String name = profile != null ? profile.getName() : null;
        String email = profile != null ? profile.getEmail() : null;
        String phone = profile != null ? profile.getPhone() : null;

        ChosenEntrantAdapter.Row.Status status;
        if (acceptedSet.contains(deviceId)) {
            status = ChosenEntrantAdapter.Row.Status.ACCEPTED;
        } else if (cancelledSet.contains(deviceId)) {
            status = ChosenEntrantAdapter.Row.Status.DECLINED;
        } else {
            status = ChosenEntrantAdapter.Row.Status.PENDING;
        }

        return new ChosenEntrantAdapter.Row(deviceId, name, email, phone, status);
    }

    /**
     * This function builds the counts of the given event of its invited, accepted, declined, and pending entrants.
     * @param event
     * @return
     */
    private SummaryCounts buildSummaryCounts(@NonNull Event event) {
        SummaryCounts counts = new SummaryCounts();

        LinkedHashSet<String> invited = new LinkedHashSet<>();
        if (event.getInvitedList() != null) {
            for (String id : event.getInvitedList()) {
                if (!TextUtils.isEmpty(id)) {
                    invited.add(id);
                }
            }
        }

        Set<String> accepted = new HashSet<>(event.getAttendeesList() != null ? event.getAttendeesList() : Collections.emptyList());
        Set<String> cancelled = new HashSet<>(event.getCanceledList() != null ? event.getCanceledList() : Collections.emptyList());

        counts.invited = invited.size();

        for (String id : invited) {
            if (accepted.contains(id)) {
                counts.accepted++;
            } else if (cancelled.contains(id)) {
                counts.declined++;
            }
        }

        counts.pending = counts.invited - counts.accepted - counts.declined;
        if (counts.pending < 0) counts.pending = 0;

        return counts;
    }

    /**
     * Update the filter summary of the given counts.
     * @param counts the counts of the entrants of the given event
     */
    private void updateFilterSummary(@NonNull SummaryCounts counts) {
        if (binding == null) return;
        String cancelledLabel = counts.declined > 0
                ? getString(R.string.chosen_filter_cancelled_with_count, counts.declined)
                : getString(R.string.chosen_filter_cancelled);
        binding.filterCancelledButton.setText(cancelledLabel);
    }

    /**
     * Apply the filter of the given event.
     */
    private void applyFilter() {
        if (binding == null) return;
        List<ChosenEntrantAdapter.Row> display = new ArrayList<>();
        if (currentFilter == FilterMode.CANCELLED) {
            for (ChosenEntrantAdapter.Row row : fullRows) {
                if (row.status == ChosenEntrantAdapter.Row.Status.DECLINED) {
                    display.add(row);
                }
            }
        } else {
            display.addAll(fullRows);
        }

        adapter.submitList(new ArrayList<>(display));
        showEmptyState(display.isEmpty());
    }

    /**
     * Get the display status of the given event.
     * @param event the event to get the display status of
     * @return the display status of the given event
     */
    private String getDisplayStatus(@NonNull Event event) {
        Event.Status status = event.getStatus();
        if (status == null) return getString(R.string.unknown);

        switch (status) {
            case REG_OPEN:
                return "Registration open";
            case REG_CLOSED:
                return "Registration closed";
            case DRAWN:
                return "Lottery drawn";
            case FINALIZED:
                return "Finalized";
            default:
                return status.name();
        }
    }

    /**
     * Called when the fragment is no longer in use.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private enum FilterMode { ALL, CANCELLED }

    private static class SummaryCounts {
        int invited;
        int accepted;
        int declined;
        int pending;
    }
}
