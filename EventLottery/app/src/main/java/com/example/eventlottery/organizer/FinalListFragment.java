package com.example.eventlottery.organizer;

import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Streamlined view that only displays confirmed attendees.
 */
public class FinalListFragment extends Fragment {

    public static final String ARG_EVENT_ID = "final_event_id";
    public static final String ARG_EVENT_TITLE = "final_event_title";
    public static final String ARG_CAN_EXPORT = "final_can_export";

    private FragmentFinalListBinding binding;
    private EventRepository eventRepository;
    private ProfileRepository profileRepository;
    private ChosenEntrantAdapter adapter;

    private String eventId;
    private String eventTitle;
    private boolean canExport = false;
    private final List<ChosenEntrantAdapter.Row> latestRows = new ArrayList<>();

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
            canExport = getArguments().getBoolean(ARG_CAN_EXPORT, false);
        }

        eventRepository = RepositoryProvider.getEventRepository();
        profileRepository = RepositoryProvider.getProfileRepository();

        adapter = new ChosenEntrantAdapter(null);
        binding.finalRecycler.setAdapter(adapter);
        binding.finalRecycler.setHasFixedSize(true);

        binding.finalEventTitle.setText(!TextUtils.isEmpty(eventTitle) ? eventTitle : "--");

        binding.exportFinalListButton.setVisibility(canExport ? View.VISIBLE : View.GONE);
        binding.exportFinalListButton.setOnClickListener(v -> exportFinalList());

        binding.finalSwipeRefresh.setOnRefreshListener(() -> {
            eventRepository.refresh();
            binding.finalSwipeRefresh.setRefreshing(false);
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

            bindHeader(event);
            loadConfirmedEntrants(event);
        });
    }

    private void bindHeader(@NonNull Event event) {
        if (binding == null) return;
        binding.finalEventTitle.setText(event.getTitle());
        int confirmed = event.getAttendeesList() != null ? event.getAttendeesList().size() : 0;
        binding.finalConfirmedHeader.setText(
                getString(R.string.final_confirmed_header, confirmed));
    }

    private void loadConfirmedEntrants(@NonNull Event event) {
        if (binding == null) return;
        List<String> attendees = event.getAttendeesList();
        LinkedHashSet<String> confirmed = new LinkedHashSet<>();
        if (attendees != null) {
            for (String id : attendees) {
                if (!TextUtils.isEmpty(id)) {
                    confirmed.add(id);
                }
            }
        }

        if (confirmed.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            showEmptyState(true);
            binding.finalSwipeRefresh.setRefreshing(false);
            binding.finalLoading.setVisibility(View.GONE);
            return;
        }

        showEmptyState(false);
        binding.finalLoading.setVisibility(View.VISIBLE);

        List<ChosenEntrantAdapter.Row> rows = new ArrayList<>();
        AtomicInteger loadedCount = new AtomicInteger(0);
        int total = confirmed.size();

        for (String deviceId : confirmed) {
            profileRepository.findUserById(deviceId, new ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(Profile profile) {
                    deliverRow(buildRow(deviceId, profile));
                }

                @Override
                public void onDeleted() {
                    deliverRow(buildRow(deviceId, null));
                }

                @Override
                public void onError(String message) {
                    deliverRow(buildRow(deviceId, null));
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

        adapter.submitList(new ArrayList<>(rows));
        latestRows.clear();
        latestRows.addAll(rows);
        binding.finalLoading.setVisibility(View.GONE);
        binding.finalSwipeRefresh.setRefreshing(false);
    }

    private ChosenEntrantAdapter.Row buildRow(@NonNull String deviceId,
                                              @Nullable Profile profile) {
        String name = profile != null ? profile.getName() : null;
        String email = profile != null ? profile.getEmail() : null;
        String phone = profile != null ? profile.getPhone() : null;

        return new ChosenEntrantAdapter.Row(
                deviceId,
                name,
                email,
                phone,
                ChosenEntrantAdapter.Row.Status.ACCEPTED
        );
    }

    private void showEmptyState(boolean show) {
        if (binding == null) return;
        binding.finalEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.finalRecycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void exportFinalList() {
        if (latestRows.isEmpty()) {
            android.widget.Toast.makeText(requireContext(),
                    R.string.export_final_list_empty, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Name,Email,Phone\n");
        for (ChosenEntrantAdapter.Row row : latestRows) {
            builder.append(escapeCsv(row.displayName != null ? row.displayName : row.deviceId))
                    .append(',')
                    .append(escapeCsv(row.email))
                    .append(',')
                    .append(escapeCsv(row.phone))
                    .append('\n');
        }

        File directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (directory == null) {
            android.widget.Toast.makeText(requireContext(),
                    R.string.export_final_list_empty, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        if (!directory.exists() && !directory.mkdirs()) {
            android.widget.Toast.makeText(requireContext(),
                    R.string.export_final_list_empty, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedTitle = TextUtils.isEmpty(eventTitle) ? "event" : eventTitle.replaceAll("[^a-zA-Z0-9_-]", "_");
        File file = new File(directory, "final_list_" + sanitizedTitle + ".csv");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            android.widget.Toast.makeText(requireContext(),
                    R.string.export_final_list_success, android.widget.Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            android.widget.Toast.makeText(requireContext(), e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
