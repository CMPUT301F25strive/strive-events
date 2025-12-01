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
import com.example.eventlottery.databinding.FragmentFinalListBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Displays the final list of invited entrants with an enrolled filter.
 */
public class FinalListFragment extends Fragment {

    public static final String ARG_EVENT_ID = "final_event_id";
    public static final String ARG_EVENT_TITLE = "final_event_title";

    private FragmentFinalListBinding binding;
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;
    private ChosenEntrantAdapter adapter;

    private String eventId;
    private String eventTitle;
    private final List<ChosenEntrantAdapter.Row> allRows = new ArrayList<>();
    private Filter currentFilter = Filter.ALL;

    private enum Filter { ALL, ENROLLED }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFinalListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventTitle = getArguments().getString(ARG_EVENT_TITLE);
        }

        eventRepository = RepositoryProvider.getEventRepository();
        profileRepository = RepositoryProvider.getProfileRepository();

        adapter = new ChosenEntrantAdapter(null);
        binding.finalRecycler.setAdapter(adapter);
        binding.finalRecycler.setHasFixedSize(true);

        binding.finalEventTitle.setText(!TextUtils.isEmpty(eventTitle) ? eventTitle : "--");

        binding.finalSwipeRefresh.setOnRefreshListener(eventRepository::refresh);

        binding.finalFilterAll.setOnClickListener(v -> {
            currentFilter = Filter.ALL;
            applyFilter();
        });
        binding.finalFilterEnrolled.setOnClickListener(v -> {
            currentFilter = Filter.ENROLLED;
            applyFilter();
        });

        observeEvents();
    }

    private void observeEvents() {
        if (TextUtils.isEmpty(eventId)) {
            showEmptyState(true);
            binding.finalSwipeRefresh.setRefreshing(false);
            return;
        }

        eventRepository.observeEvents().observe(getViewLifecycleOwner(), events -> {
            Event event = eventRepository.findEventById(eventId);
            if (event == null) {
                showEmptyState(true);
                binding.finalSwipeRefresh.setRefreshing(false);
                return;
            }

            bindSummary(event);
            fetchRows(event);
        });
    }

    private void bindSummary(@NonNull Event event) {
        List<String> invited = event.getInvitedList();
        List<String> enrolledList = event.getAttendeesList();

        int invitedCount = invited != null ? invited.size() : 0;
        int enrolledCount = enrolledList != null ? enrolledList.size() : 0;
        int pendingCount = invitedCount - enrolledCount;

        binding.finalInvitedValue.setText(String.format(Locale.getDefault(), "%d", invitedCount));
        binding.finalEnrolledValue.setText(String.format(Locale.getDefault(), "%d", enrolledCount));
        binding.finalPendingValue.setText(String.format(Locale.getDefault(), "%d", Math.max(pendingCount, 0)));

        binding.finalFilterEnrolled.setText(
                getString(R.string.final_filter_enrolled_with_count, enrolledCount)
        );
    }

    private void fetchRows(@NonNull Event event) {
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
            allRows.clear();
            adapter.submitList(new ArrayList<>());
            showEmptyState(true);
            return;
        }

        showEmptyState(false);
        binding.finalLoading.setVisibility(View.VISIBLE);

        Set<String> acceptedSet = new HashSet<>(event.getAttendeesList() != null
                ? event.getAttendeesList() : new ArrayList<>());
        Set<String> cancelledSet = new HashSet<>(event.getCanceledList() != null
                ? event.getCanceledList() : new ArrayList<>());

        List<ChosenEntrantAdapter.Row> rows = new ArrayList<>();
        int total = invitedUnique.size();
        AtomicInteger loadedCount = new AtomicInteger(0);

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
        rows.sort((left, right) -> {
            String leftName = left.displayName != null ? left.displayName : "";
            String rightName = right.displayName != null ? right.displayName : "";
            return leftName.compareToIgnoreCase(rightName);
        });

        allRows.clear();
        allRows.addAll(rows);
        binding.finalLoading.setVisibility(View.GONE);
        binding.finalSwipeRefresh.setRefreshing(false);
        applyFilter();
    }

    private ChosenEntrantAdapter.Row buildRow(@NonNull String deviceId,
                                              @Nullable Profile profile,
                                              @NonNull Set<String> accepted,
                                              @NonNull Set<String> cancelled) {
        String name = profile != null ? profile.getName() : null;
        String email = profile != null ? profile.getEmail() : null;
        String phone = profile != null ? profile.getPhone() : null;

        ChosenEntrantAdapter.Row.Status status;
        if (accepted.contains(deviceId)) {
            status = ChosenEntrantAdapter.Row.Status.ACCEPTED;
        } else if (cancelled.contains(deviceId)) {
            status = ChosenEntrantAdapter.Row.Status.DECLINED;
        } else {
            status = ChosenEntrantAdapter.Row.Status.PENDING;
        }

        return new ChosenEntrantAdapter.Row(deviceId, name, email, phone, status);
    }

    private void applyFilter() {
        List<ChosenEntrantAdapter.Row> display = new ArrayList<>();

        if (currentFilter == Filter.ENROLLED) {
            for (ChosenEntrantAdapter.Row row : allRows) {
                if (row.status == ChosenEntrantAdapter.Row.Status.ACCEPTED) {
                    display.add(row);
                }
            }
        } else {
            display.addAll(allRows);
        }

        adapter.submitList(new ArrayList<>(display));
        showEmptyState(display.isEmpty());
    }

    private void showEmptyState(boolean empty) {
        if (binding == null) return;
        binding.finalEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.finalRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            binding.finalLoading.setVisibility(View.GONE);
        }
        binding.finalSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
