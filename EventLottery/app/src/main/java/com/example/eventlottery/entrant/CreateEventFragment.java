package com.example.eventlottery.ui.createevent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eventlottery.R;
import com.example.eventlottery.data.FirebaseEventRepository;
import com.example.eventlottery.model.Event;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.Locale;

public class CreateEventFragment extends Fragment {

    private static final int PICK_IMAGE = 10;

    private ImageView eventPoster;
    private Uri imageUri;

    private TextInputEditText editTextTitle, editTextDescription, editTextLocation,
            editTextEventDate, editTextEventTime, editTextMaxParticipants,
            editTextRegStartDate, editTextRegStartTime,
            editTextRegEndDate, editTextRegEndTime;
    private TextView charCountText;

    private Button saveButton;
    private ImageButton backButton;
    private ChipGroup tagChipGroup;
    private Event.Tag selectedTag = null;

    private FirebaseEventRepository firebaseRepo;

    private final Calendar eventCalendar = Calendar.getInstance();
    private final Calendar regStartCalendar = Calendar.getInstance();
    private final Calendar regEndCalendar = Calendar.getInstance();

    public CreateEventFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        firebaseRepo = new FirebaseEventRepository();

        eventPoster = view.findViewById(R.id.eventPoster);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextLocation = view.findViewById(R.id.editTextLocation);
        editTextEventDate = view.findViewById(R.id.editTextEventDate);
        editTextEventTime = view.findViewById(R.id.editTextEventTime);
        editTextRegStartDate = view.findViewById(R.id.editTextRegStartDate);
        editTextRegStartTime = view.findViewById(R.id.editTextRegStartTime);
        editTextRegEndDate   = view.findViewById(R.id.editTextRegEndDate);
        editTextRegEndTime   = view.findViewById(R.id.editTextRegEndTime);

        editTextMaxParticipants = view.findViewById(R.id.editTextMaxParticipants);
        charCountText = view.findViewById(R.id.charCountText);

        saveButton = view.findViewById(R.id.buttonSaveEvent);
        backButton = view.findViewById(R.id.backButton);
        tagChipGroup = view.findViewById(R.id.tagChipGroupForCreating);

        // Tag selection
        tagChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedTag = null;
                return;
            }

            int id = checkedIds.get(0);

            if (id == R.id.chip_art) {
                selectedTag = Event.Tag.ART;
            } else if (id == R.id.chip_music) {
                selectedTag = Event.Tag.MUSIC;
            } else if (id == R.id.chip_education) {
                selectedTag = Event.Tag.EDUCATION;
            } else if (id == R.id.chip_sports) {
                selectedTag = Event.Tag.SPORTS;
            } else if (id == R.id.chip_party) {
                selectedTag = Event.Tag.PARTY;
            } else {
                selectedTag = null;
            }
        });

        eventPoster.setOnClickListener(v -> openGallery());
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Description
        editTextDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCountText.setText(s.length() + " / 200");
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Event start
        editTextEventDate.setOnClickListener(v ->
                showDatePicker(editTextEventDate, eventCalendar));
        editTextEventTime.setOnClickListener(v ->
                showTimePicker(editTextEventTime, editTextEventDate, eventCalendar));

        // Registration starts
        editTextRegStartDate.setOnClickListener(v ->
                showDatePicker(editTextRegStartDate, regStartCalendar));
        editTextRegStartTime.setOnClickListener(v ->
                showTimePicker(editTextRegStartTime, editTextRegStartDate, regStartCalendar));

        // Registration ends
        editTextRegEndDate.setOnClickListener(v ->
                showDatePicker(editTextRegEndDate, regEndCalendar));
        editTextRegEndTime.setOnClickListener(v ->
                showTimePicker(editTextRegEndTime, editTextRegEndDate, regEndCalendar));

        // Save button
        saveButton.setOnClickListener(v -> saveEvent(view));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();
            eventPoster.setImageURI(imageUri);
        }
    }

    /**
     * Generic date picker used for dates of event and its registration period
     * @param targetDateField: the TextInputEditText object
     * @param targetCalendar: the Calendar object
     */
    private void showDatePicker(TextInputEditText targetDateField,
                                @Nullable Calendar targetCalendar) {
        Calendar now = Calendar.getInstance();

        // Create and configure the date picker dialog
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    if (targetCalendar != null) {
                        // Update the target calendar with the selected date
                        targetCalendar.set(Calendar.YEAR, year);
                        targetCalendar.set(Calendar.MONTH, month);
                        targetCalendar.set(Calendar.DAY_OF_MONTH, day);
                    }

                    // Format the selected date
                    String formatted = String.format(
                            Locale.getDefault(),    // Use device's locale for formatting
                            "%02d/%02d/%04d",   // Format: DD/MM/YYYY
                            day, month + 1, year    // month is 0-based
                    );
                    targetDateField.setText(formatted);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        // Disallow all dates before today
        datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
        datePicker.show();
    }

    /**
     * Generic time picker used for time of event and registration period
     * @param targetTimeField: the TextInputEditText object for time
     * @param relatedDateField: the TextInputEditText object for date
     * @param targetCalendar: the Calendar object
     */
    private void showTimePicker(TextInputEditText targetTimeField,
                                TextInputEditText relatedDateField,
                                @Nullable Calendar targetCalendar) {

        // Get current time to use as default values
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);    // 24-hour format (0-23)
        int currentMinute = now.get(Calendar.MINUTE);

        // Read the selected date to check if it's the current day (today)
        Calendar selectedDate = Calendar.getInstance();
        String dateText = relatedDateField.getText() != null
                ? relatedDateField.getText().toString().trim() : "";

        // Parse the string of date
        if (!dateText.isEmpty()) {
            String[] parts = dateText.split("/");
            if (parts.length == 3) {
                try {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1; // 0-based month
                    int year = Integer.parseInt(parts[2]);
                    selectedDate.set(year, month, day);
                } catch (NumberFormatException ignored) {
                    // If fail to parse, default to current date
                    selectedDate.setTime(now.getTime());
                }
            } else {
                // If the date format is invalid, default to current date
                selectedDate.setTime(now.getTime());
            }
        } else {
            // If no date selected, default to current date
            selectedDate.setTime(now.getTime());
        }

        // Create and configure the time picker dialog
        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {

                    // Check if the selected date is today
                    boolean isToday = selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            && selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);

                    // If the selected date is today, block past times
                    if (isToday &&
                            (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute))
                    ) {
                        Toast.makeText(requireContext(),
                                "Cannot select past time", Toast.LENGTH_SHORT).show();
                        return; // Cancel selection and exit
                    }

                    if (targetCalendar != null) {
                        // Update the target calendar with the selected time
                        targetCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        targetCalendar.set(Calendar.MINUTE, selectedMinute);
                        targetCalendar.set(Calendar.SECOND, 0);
                        targetCalendar.set(Calendar.MILLISECOND, 0);
                    }

                    // Format the selected time
                    String formatted = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",    // Format: HH:MM (24-hour format)
                            selectedHour, selectedMinute
                    );
                    targetTimeField.setText(formatted);
                },
                currentHour,
                currentMinute,
                true   // 24-hour format (true) or 12-hour AM/PM format (false)
        );

        timePicker.show();
    }

    private void saveEvent(View root) {
        // Validation steps
        String title = editTextTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String eventDate = editTextEventDate.getText().toString().trim();
        String eventTime = editTextEventTime.getText().toString().trim();
        String max = editTextMaxParticipants.getText().toString().trim();
        int maxParticipants = max.isEmpty() ? 0 : Integer.parseInt(max);

        if (selectedTag == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventDate.isEmpty() || eventTime.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Event date and time are required", Toast.LENGTH_SHORT).show();
            return;
        }
        long eventStartTimeMillis = eventCalendar.getTimeInMillis();

        String regStartDateStr = editTextRegStartDate.getText() != null
                ? editTextRegStartDate.getText().toString().trim() : "";
        String regStartTimeStr = editTextRegStartTime.getText() != null
                ? editTextRegStartTime.getText().toString().trim() : "";
        String regEndDateStr = editTextRegEndDate.getText() != null
                ? editTextRegEndDate.getText().toString().trim() : "";
        String regEndTimeStr = editTextRegEndTime.getText() != null
                ? editTextRegEndTime.getText().toString().trim() : "";

        long regStartTimeMillis;
        long regEndTimeMillis;

        if (regStartDateStr.isEmpty() || regStartTimeStr.isEmpty()) {
            // If registration start not specified, default to now
            regStartTimeMillis = System.currentTimeMillis();
            regStartCalendar.setTimeInMillis(regStartTimeMillis);
        } else {
            regStartTimeMillis = regStartCalendar.getTimeInMillis();
        }

        if (regEndDateStr.isEmpty() || regEndTimeStr.isEmpty()) {
            // If registration end not specified, default to event start
            regEndTimeMillis = eventStartTimeMillis;
            regEndCalendar.setTimeInMillis(regEndTimeMillis);
        } else {
            regEndTimeMillis = regEndCalendar.getTimeInMillis();
        }

        // Enforce regStart <= regEnd <= eventStart
        if (regEndTimeMillis > eventStartTimeMillis) {
            regEndTimeMillis = eventStartTimeMillis;
        }
        if (regStartTimeMillis > regEndTimeMillis) {
            regStartTimeMillis = regEndTimeMillis;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Posting...");

        // DEVICE ID
        String deviceId =
                Settings.Secure.getString(requireContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);

        firebaseRepo.uploadEvent(
                imageUri,
                title,
                description,
                location,
                eventStartTimeMillis,
                regStartTimeMillis,
                regEndTimeMillis,
                maxParticipants,
                deviceId,
                selectedTag,
                (success, message) -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Post");
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        Navigation.findNavController(root).navigateUp();
                    }
                }
        );
    }
}
