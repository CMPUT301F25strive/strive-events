package com.example.eventlottery.organizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentWaitingListPanelBinding;
import com.example.eventlottery.entrant.WaitingListAdapter;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight waiting list view scoped to the organizer tab.
 */
public class OrganizerWaitingListFragment extends Fragment {

    public static final String ARG_EVENT_ID = "waiting_event_id";
    public static final String ARG_EVENT_TITLE = "waiting_event_title";

    private FragmentWaitingListPanelBinding binding;
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;
    private WaitingListAdapter adapter;

    private String eventId;
    private String initialTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWaitingListPanelBinding.inflate(inflater, container, false);
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

        adapter = new WaitingListAdapter();
        binding.waitingListRecycler.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        binding.waitingListRecycler.setAdapter(adapter);
        binding.waitingListRecycler.setHasFixedSize(true);

        if (!TextUtils.isEmpty(initialTitle)) {
            binding.waitingListEventTitle.setText(initialTitle);
        }

        binding.waitingListSwipeRefresh.setOnRefreshListener(() -> eventRepository.refresh());

        observeEvent();
    }

    private void observeEvent() {
        if (TextUtils.isEmpty(eventId)) {
            showEmptyState(true);
            binding.waitingListCount.setText("--");
            binding.waitingListSwipeRefresh.setEnabled(false);
            return;
        }

        eventRepository.observeEvents().observe(getViewLifecycleOwner(), events -> {
            Event event = eventRepository.findEventById(eventId);
            if (event == null) {
                showEmptyState(true);
                binding.waitingListCount.setText("0 entrants waiting");
                binding.waitingListSwipeRefresh.setRefreshing(false);
                return;
            }

            bindSummary(event);
            loadProfiles(event.getWaitingList());
        });
    }

    private void bindSummary(@NonNull Event event) {
        binding.waitingListEventTitle.setText(event.getTitle());
        int count = event.getWaitingList() != null ? event.getWaitingList().size() : 0;
        binding.waitingListCount.setText(count + " entrants waiting");
    }

    private void loadProfiles(@Nullable List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            adapter.submitList(Collections.emptyList());
            showEmptyState(true);
            binding.waitingListLoading.setVisibility(View.GONE);
            binding.waitingListSwipeRefresh.setRefreshing(false);
            return;
        }

        showEmptyState(false);
        binding.waitingListLoading.setVisibility(View.VISIBLE);

        List<Profile> profiles = new ArrayList<>();
        AtomicInteger loaded = new AtomicInteger(0);
        int total = userIds.size();

        for (String uid : userIds) {
            profileRepository.findUserById(uid, new ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(Profile profile) {
                    if (profile != null) {
                        profiles.add(profile);
                    }
                    handleComplete();
                }

                @Override
                public void onDeleted() {
                    handleComplete();
                }

                @Override
                public void onError(String message) {
                    handleComplete();
                }

                private void handleComplete() {
                    if (loaded.incrementAndGet() == total && isAdded()) {
                        requireActivity().runOnUiThread(() -> renderProfiles(profiles));
                    }
                }
            });
        }
    }

    private void renderProfiles(@NonNull List<Profile> profiles) {
        Collections.sort(profiles, (a, b) -> {
            String left = a.getName() != null ? a.getName() : "";
            String right = b.getName() != null ? b.getName() : "";
            return left.compareToIgnoreCase(right);
        });

        adapter.submitList(new ArrayList<>(profiles));
        binding.waitingListLoading.setVisibility(View.GONE);
        binding.waitingListSwipeRefresh.setRefreshing(false);
        showEmptyState(profiles.isEmpty());
    }

    private void showEmptyState(boolean show) {
        binding.waitingListEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
