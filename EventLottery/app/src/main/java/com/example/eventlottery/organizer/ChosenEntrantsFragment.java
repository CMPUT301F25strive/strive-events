package com.example.eventlottery.organizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentChosenEntrantsBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

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

    private FragmentChosenEntrantsBinding binding;
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;
    private ChosenEntrantAdapter adapter;

    private String eventId;
    private String initialTitle;
    private Event currentEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChosenEntrantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventRepository = RepositoryProvider.getEventRepository();
        profileRepository = RepositoryProvider.getProfileRepository();

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            initialTitle = getArguments().getString(ARG_EVENT_TITLE);
        }

        adapter = new ChosenEntrantAdapter();
        binding.chosenEntrantsRecycler.setAdapter(adapter);
        binding.chosenEntrantsRecycler.setHasFixedSize(true);

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

    private void bindSummary(@NonNull Event event) {
        if (binding == null) return;
        binding.summaryEventTitle.setText(event.getTitle());
        binding.summaryEventStatus.setText(getDisplayStatus(event));

        SummaryCounts counts = buildSummaryCounts(event);
        binding.invitedCountValue.setText(String.format(Locale.getDefault(), "%d", counts.invited));
        binding.acceptedCountValue.setText(String.format(Locale.getDefault(), "%d", counts.accepted));
        binding.pendingCountValue.setText(String.format(Locale.getDefault(), "%d", counts.pending));
    }

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

    private void renderRows(@NonNull List<ChosenEntrantAdapter.Row> rows) {
        if (binding == null) return;
        Collections.sort(rows, (left, right) -> {
            String leftName = left.displayName != null ? left.displayName : "";
            String rightName = right.displayName != null ? right.displayName : "";
            return leftName.compareToIgnoreCase(rightName);
        });

        adapter.submitList(new ArrayList<>(rows));
        showLoading(false);
        binding.swipeRefresh.setRefreshing(false);
        showEmptyState(rows.isEmpty());
    }

    private void showLoading(boolean show) {
        if (binding == null) return;
        binding.loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        if (binding == null) return;
        binding.emptyStateGroup.setVisibility(show ? View.VISIBLE : View.GONE);
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class SummaryCounts {
        int invited;
        int accepted;
        int declined;
        int pending;
    }
}
