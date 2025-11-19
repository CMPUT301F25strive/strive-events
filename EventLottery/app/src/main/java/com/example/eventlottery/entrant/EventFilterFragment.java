package com.example.eventlottery.entrant;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.EventFilter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.Locale;


public class EventFilterFragment extends DialogFragment {
    private static final String ARG_FILTER = "arg_filter";  // Constant key for the EventFilter arg Bundle.

    public interface Listener {
        void onFilterChanged(@NonNull EventFilter filter);
    }

    // Listener to parent (EntrantEventListFragment)
    private Listener listener;

    // UI
    private ChipGroup chipGroup;
    private MaterialButton startDateBtn;
    private MaterialButton endDateBtn;

    // Filter state
    @Nullable
    private Long startTime = null;
    @Nullable
    private Long endTime = null;
    @Nullable
    private Event.Tag selectedTag = null;
    @Nullable
    private EventFilter initialFilter;

    public static EventFilterFragment newInstance(@Nullable EventFilter filter) {
        EventFilterFragment fragment = new EventFilterFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTER, filter);   // Pack current filter into bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment parent = getParentFragment();
        if (parent instanceof Listener) {
            listener = (Listener) parent;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialFilter = (EventFilter) getArguments().getSerializable(ARG_FILTER);
            if (initialFilter != null) {
                startTime = initialFilter.getFilterStartTimeMillis();
                endTime = initialFilter.getFilterEndTimeMillis();
                selectedTag = initialFilter.getFilterTag();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null || getDialog().getWindow() == null) return;

        Window window = getDialog().getWindow();
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 50; // Offset from status bar
        window.setAttributes(params);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_filter, container, false);

        chipGroup = view.findViewById(R.id.tagChipGroupForFiltering);
        startDateBtn = view.findViewById(R.id.startDateButton);
        endDateBtn = view.findViewById(R.id.endDateButton);
        ImageButton closeBtn = view.findViewById(R.id.closeButton);
        TextView resetBtn = view.findViewById(R.id.resetButton);

        closeBtn.setOnClickListener(v -> dismiss());
        resetBtn.setOnClickListener(v -> resetFilter());

        // Connect behavior to internal states (the filter)
        setupChips();
        setupDateButtons();

        // Set UI to match internal state (the filter)
        bindInitialStateToUI();

        return view;
    }

    // Event tag listener
    private void setupChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedTag = null;
            } else {
                int id = checkedIds.get(0);
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    String text = chip.getText().toString();
                    selectedTag = mapChipTextToTag(text);
                } else {
                    selectedTag = null;
                }
            }

            sendFilter();   // Notify parent
        });
    }

    private Event.Tag mapChipTextToTag(String text) {
        switch (text.toUpperCase()) {
            case "ART": return Event.Tag.ART;
            case "MUSIC": return Event.Tag.MUSIC;
            case "EDUCATION": return Event.Tag.EDUCATION;
            case "SPORTS": return Event.Tag.SPORTS;
            case "PARTY": return Event.Tag.PARTY;
        }
        return null;
    }

    // Time frame listener
    private void setupDateButtons() {
        startDateBtn.setOnClickListener(v -> showDatePicker(true));
        endDateBtn.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStart) {
        final Calendar now = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> {
                    Calendar c = Calendar.getInstance();

                    if (isStart) {
                        // Start of the selected day
                        c.set(year, month, day, 0, 0, 0);
                        long millis = c.getTimeInMillis();

                        startTime = millis;
                        startDateBtn.setText(format(year, month, day));
                    } else {
                        // End of the selected day (inclusive)
                        c.set(year, month, day, 23, 59, 59);
                        long millis = c.getTimeInMillis();

                        // Validate: end date cannot be before start date
                        if (startTime != null && millis < startTime) {
                            Toast.makeText(
                                    requireContext(),
                                    "End date cannot be before start date",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        endTime = millis;
                        endDateBtn.setText(format(year, month, day));
                    }

                    sendFilter();   // Notify parent
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private String format(int y, int m, int d) {
        return String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
    }

    private void sendFilter() {
        if (listener == null) return;

        EventFilter filter = new EventFilter(startTime, endTime, selectedTag);
        listener.onFilterChanged(filter);
    }

    private void bindInitialStateToUI() {
        // Tag selection
        if (selectedTag != null && chipGroup != null) {
            // If with a selected tag chip form last time, match the id
            int chipId = View.NO_ID;
            switch (selectedTag) {
                case ART: chipId = R.id.chip_art; break;
                case MUSIC: chipId = R.id.chip_music; break;
                case EDUCATION: chipId = R.id.chip_education; break;
                case SPORTS: chipId = R.id.chip_sports; break;
                case PARTY: chipId = R.id.chip_party; break;
            }
            if (chipId != View.NO_ID) {
                // Check the matched chip
                chipGroup.check(chipId);
            }
        } else if (chipGroup != null) {
            // If no tag selected, clear the selection.
            chipGroup.clearCheck();
        }

        // Start date
        if (startTime != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startTime);
            startDateBtn.setText(format(
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ));
        } else {
            startDateBtn.setText("Start date");
        }

        // End date
        if (endTime != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(endTime);
            endDateBtn.setText(format(
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ));
        } else {
            endDateBtn.setText("End date");
        }
    }

    private void resetFilter() {
        // Clear filter state
        startTime = null;
        endTime = null;
        selectedTag = null;

        // Clear UI
        if (chipGroup != null) {
            chipGroup.clearCheck();
        }
        startDateBtn.setText("Start date");
        endDateBtn.setText("End date");

        // Notify parent
        sendFilter();
    }
}
