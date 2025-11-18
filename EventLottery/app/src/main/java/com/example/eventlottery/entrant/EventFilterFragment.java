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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.R;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.EventFilter;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.Locale;


public class EventFilterFragment extends DialogFragment {

    public interface Listener {
        void onFilterChanged(@NonNull EventFilter filter);
    }

    // Initializations
    private Listener listener;
    private ChipGroup chipGroup;
    private MaterialButton startDateBtn;
    private MaterialButton endDateBtn;

    // filter state
    private Long startTime = null;
    private Long endTime = null;
    private Event.Tag selectedTag = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment parent = getParentFragment();
        if (parent instanceof Listener) {
            listener = (Listener) parent;
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
        params.y = 50; // Offset from the top
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

        closeBtn.setOnClickListener(v -> dismiss());

        setupChips();
        setupDateButtons();

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
                String text = chip.getText().toString();

                selectedTag = mapChipTextToTag(text);
            }

            sendFilter();
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
                    c.set(year, month, day, 0, 0, 0);

                    long millis = c.getTimeInMillis();

                    if (isStart) {
                        startTime = millis;
                        startDateBtn.setText(format(year, month, day));
                    } else {
                        endTime = millis;
                        endDateBtn.setText(format(year, month, day));
                    }

                    sendFilter();
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
}
