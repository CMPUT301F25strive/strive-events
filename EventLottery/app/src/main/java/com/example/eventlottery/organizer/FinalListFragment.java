package com.example.eventlottery.organizer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.example.eventlottery.databinding.FragmentFinalListBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.Profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's  UI should be attached to.  The fragment should not add the view itself,
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFinalListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after OnCreateView has returned,but before any saved state has been restored in to
     * @param view The View returned by OnCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
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
        binding.finalRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
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

    /**
     * Find the event with its id and update the UI, show the summary of the given event.
     */
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

    /**
     * This function binds the header of the given event.
     * @param event The event to bind the header of.
     */
    private void bindHeader(@NonNull Event event) {
        if (binding == null) return;
        binding.finalEventTitle.setText(event.getTitle());
        int confirmed = event.getAttendeesList() != null ? event.getAttendeesList().size() : 0;
        binding.finalConfirmedHeader.setText(
                getString(R.string.final_confirmed_header, confirmed));
    }

    /**
     * Load the confirmed entrants of the given event.
     * @param event the event to load the confirmed entrants of.
     */
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

    /**
     * Render the rows of confirmed entrants with the given list.
     * @param rows the list of confirmed entrants
     */
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

    /**
     * Build the row of confirmed entrants with the given parameters.
     * @param deviceId the id of the entrant
     * @param profile the profile of the entrant
     * @return the row of confirmed entrants
     */
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

    /**
     * Set the visibility of the empty state.
     * @param show the visibility of the empty state
     */
    private void showEmptyState(boolean show) {
        if (binding == null) return;
        binding.finalEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.finalRecycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Export the final list of confirmed entrants.
     */
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

        String sanitizedTitle = TextUtils.isEmpty(eventTitle) ? "event" : eventTitle.replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = "final_list_" + sanitizedTitle + ".csv";
        String csvContent = builder.toString();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = requireContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri item = resolver.insert(collection, values);
            if (item == null) {
                android.widget.Toast.makeText(requireContext(),
                        R.string.export_final_list_error, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = false;
            try (OutputStream outputStream = resolver.openOutputStream(item)) {
                if (outputStream != null) {
                    outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
                    success = true;
                }
            } catch (IOException e) {
                resolver.delete(item, null, null);
                android.widget.Toast.makeText(requireContext(), e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(item, values, null, null);

            if (success) {
                android.widget.Toast.makeText(requireContext(),
                        R.string.export_final_list_success, android.widget.Toast.LENGTH_SHORT).show();
            }
        } else {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (directory == null) {
                android.widget.Toast.makeText(requireContext(),
                        R.string.export_final_list_error, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (!directory.exists() && !directory.mkdirs()) {
                android.widget.Toast.makeText(requireContext(),
                        R.string.export_final_list_error, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(directory, fileName);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
                android.widget.Toast.makeText(requireContext(),
                        R.string.export_final_list_success, android.widget.Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                android.widget.Toast.makeText(requireContext(), e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sanitize the given value for CSV.
     * @param value the value to sanitize
     * @return the sanitized value
     */
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
