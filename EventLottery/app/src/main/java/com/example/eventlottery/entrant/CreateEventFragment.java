package com.example.eventlottery.ui.createevent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.eventlottery.R;
import com.example.eventlottery.data.FirebaseEventRepository;
import com.example.eventlottery.model.Event;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.UUID;

public class CreateEventFragment extends Fragment {

    private static final int PICK_IMAGE = 10;

    private ImageView eventPoster;
    private Uri imageUri;

    private TextInputEditText editTextTitle, editTextDescription, editTextLocation,
            editTextDate, editTextTime, editTextMaxParticipants;
    private TextView charCountText;

    private Button saveButton;
    private ImageButton backButton;
    private ChipGroup tagChipGroup;
    private Event.Tag selectedTag = null;

    private FirebaseEventRepository firebaseRepo;

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
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextTime = view.findViewById(R.id.editTextTime);
        editTextMaxParticipants = view.findViewById(R.id.editTextMaxParticipants);
        charCountText = view.findViewById(R.id.charCountText);

        saveButton = view.findViewById(R.id.buttonSaveEvent);
        backButton = view.findViewById(R.id.backButton);
        tagChipGroup = view.findViewById(R.id.tagChipGroupForCreating);

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

        // === IMAGE PICKER ===
        eventPoster.setOnClickListener(v -> openGallery());

        // === BACK BUTTON ===
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // === CHAR COUNT ===
        editTextDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCountText.setText(s.length() + " / 200");
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // === DATE PICKER ===
        editTextDate.setOnClickListener(v -> showDatePicker());

        // === TIME PICKER ===
        editTextTime.setOnClickListener(v -> showTimePicker());

        // === SAVE EVENT ===
        saveButton.setOnClickListener(v -> saveEvent(view));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
    private void showDatePicker() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> editTextDate.setText(day + "/" + (month + 1) + "/" + year),
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        // Restrict past dates (today or later)
        datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
        datePicker.show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        // Determine selected date
        Calendar selectedDate = Calendar.getInstance();
        String dateText = editTextDate.getText().toString();
        if (!dateText.isEmpty()) {
            String[] parts = dateText.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);
            selectedDate.set(year, month, day);
        }

        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    // If selected date is today, block past times
                    if (selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            && selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                        if (selectedHour < hour || (selectedHour == hour && selectedMinute < minute)) {
                            Toast.makeText(requireContext(), "Cannot select past time", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    editTextTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                },
                hour,
                minute,
                false
        );

        timePicker.show();
    }

    private void saveEvent(View root) {
        String title = editTextTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String max = editTextMaxParticipants.getText().toString().trim();
        int maxParticipants = max.isEmpty() ? 0 : Integer.parseInt(max);

        // Select a tag
        if (selectedTag == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), "Date and Time are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Compute startTimeMillis
        long startTimeMillis = 0L;
        try {
            String dateTimeStr = date + " " + time; // "17/11/2025 14:30"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy HH:mm");
            java.util.Date parsedDate = sdf.parse(dateTimeStr);
            if (parsedDate != null) {
                startTimeMillis = parsedDate.getTime();
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Invalid date/time", Toast.LENGTH_SHORT).show();
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Posting...");

        firebaseRepo.uploadEvent(imageUri, title, description, location, date, time, maxParticipants, selectedTag,
                (success, message) -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Post");
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        Navigation.findNavController(root).navigateUp();
                    }
                });

    }
}