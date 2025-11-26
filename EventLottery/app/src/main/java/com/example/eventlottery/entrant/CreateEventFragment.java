package com.example.eventlottery.entrant;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.FirebaseEventRepository;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.EventTimeValidator;
import com.example.eventlottery.organizer.OrganizerGate;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.ChipGroup;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Calendar;
import java.util.Locale;

public class CreateEventFragment extends Fragment {

    private static final int PICK_IMAGE = 10;

    private ImageView eventPoster;
    private Uri imageUri;

    private TextInputEditText editTextTitle, editTextDescription, editTextLocation,
            editTextEventDate, editTextEventTime,
            editTextRegStartDate, editTextRegStartTime,
            editTextRegEndDate, editTextRegEndTime,
            editTextCapacity, editTextWaitingListSpots;
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
        if (!OrganizerGate.hasOrganizerAccess(requireContext())) {
            Toast.makeText(requireContext(), R.string.organizer_feature_required, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        firebaseRepo = new FirebaseEventRepository();

        initViews(view);
        setupTagSelection();
        setupListeners();
    }

    private void initViews(View view) {
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
        editTextCapacity = view.findViewById(R.id.editTextCapacity);
        editTextWaitingListSpots = view.findViewById(R.id.editTextWaitingListSpots);
        charCountText = view.findViewById(R.id.charCountText);
        saveButton = view.findViewById(R.id.buttonSaveEvent);
        backButton = view.findViewById(R.id.backButton);
        tagChipGroup = view.findViewById(R.id.tagChipGroupForCreating);
    }

    private void setupTagSelection() {
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
    }

    private void setupListeners() {
        eventPoster.setOnClickListener(v -> openGallery());
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        editTextDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCountText.setText(s.length() + " / 200");
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Event date/time
        editTextEventDate.setOnClickListener(v -> showDatePicker(editTextEventDate, editTextEventTime, eventCalendar));
        editTextEventTime.setOnClickListener(v -> showTimePicker(editTextEventTime, editTextEventDate, eventCalendar));

        // Registration date/time
        editTextRegStartDate.setOnClickListener(v -> showDatePicker(editTextRegStartDate, editTextRegStartTime, regStartCalendar));
        editTextRegStartTime.setOnClickListener(v -> showTimePicker(editTextRegStartTime, editTextRegStartDate, regStartCalendar));
        editTextRegEndDate.setOnClickListener(v -> showDatePicker(editTextRegEndDate, editTextRegEndTime, regEndCalendar));
        editTextRegEndTime.setOnClickListener(v -> showTimePicker(editTextRegEndTime, editTextRegEndDate, regEndCalendar));

        saveButton.setOnClickListener(v -> saveEvent(v));
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

    private void showDatePicker(TextInputEditText dateField, TextInputEditText timeField, Calendar calendar) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                    dateField.setText(formatted);
                    if (timeField.getText().toString().trim().isEmpty()) {
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        timeField.setText("00:00");
                    }
                },
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
        datePicker.show();
    }

    private void showTimePicker(TextInputEditText timeField, TextInputEditText dateField, Calendar calendar) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        Calendar selectedDate = Calendar.getInstance();
        String dateText = dateField.getText().toString().trim();
        if (!dateText.isEmpty()) {
            String[] parts = dateText.split("/");
            try {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);
                selectedDate.set(year, month, day);
            } catch (NumberFormatException ignored) {
                selectedDate.setTime(now.getTime());
            }
        }

        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    boolean isToday = selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            && selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);

                    if (isToday && (selectedHour < hour || (selectedHour == hour && selectedMinute < minute))) {
                        Toast.makeText(requireContext(), "Cannot select past time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);

                    timeField.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));

                    if (dateField.getText().toString().trim().isEmpty()) {
                        dateField.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                now.get(Calendar.DAY_OF_MONTH),
                                now.get(Calendar.MONTH) + 1,
                                now.get(Calendar.YEAR)));
                        calendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                    }
                }, hour, minute, false);
        timePicker.show();
    }

    private void saveEvent(View root) {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "Title, description, and location are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTag == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        long eventStartTime = eventCalendar.getTimeInMillis();
        long now = System.currentTimeMillis();
        long regStart = regStartCalendar.getTimeInMillis();
        long regEnd = regEndCalendar.getTimeInMillis();

        if (!EventTimeValidator.isEventDateValid(Calendar.getInstance(), eventCalendar)) {
            Toast.makeText(requireContext(),
                    "Event must start at least " + EventTimeValidator.MIN_EVENT_LEAD_DAYS + " days from today.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (regStart < now) regStart = now;
        if (regEnd <= regStart || eventStartTime - regEnd < EventTimeValidator.MIN_REG_DRAWN_GAP_MILLIS) {
            Toast.makeText(requireContext(),
                    "Invalid registration period. Registration must close at least 24h before event starts.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        int capacity;
        try { capacity = Integer.parseInt(editTextCapacity.getText().toString().trim()); }
        catch (NumberFormatException e) { Toast.makeText(requireContext(), "Invalid capacity", Toast.LENGTH_SHORT).show(); return; }

        int waitingList = -1;
        String waitingListStr = editTextWaitingListSpots.getText().toString().trim();
        if (!waitingListStr.isEmpty()) {
            try { waitingList = Integer.parseInt(waitingListStr); }
            catch (NumberFormatException e) { Toast.makeText(requireContext(), "Invalid waiting list spots", Toast.LENGTH_SHORT).show(); return; }
        }

        saveButton.setEnabled(false);
        saveButton.setText("Posting...");

        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        firebaseRepo.uploadEvent(imageUri, title, description, location, eventStartTime,
                regStart, regEnd, capacity, waitingList, deviceId, selectedTag, new FirebaseEventRepository.UploadCallback() {
                    @Override
                    public void onProgress(double progress) {
                        saveButton.setText(String.format("Posting... %.0f%%", progress * 100));
                    }

                    @Override
                    public void onComplete(boolean success, String message, String eventID) {
                        saveButton.setEnabled(true);
                        saveButton.setText("Post");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        if (success && eventID != null) {
                            Navigation.findNavController(root).navigateUp();
                            generateQRCode(eventID);
                        }
                    }
                });
    }

    private void generateQRCode(String eventID) {
        try {
            String qrLink = "event/" + eventID;
            int size = 600;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrLink, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            showQrDialog(bitmap);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "QR generation failed: " + e, Toast.LENGTH_LONG).show();
        }
    }

    private void showQrDialog(Bitmap qrBitmap) {
        ImageView image = new ImageView(requireContext());
        image.setImageBitmap(qrBitmap);
        image.setPadding(50, 50, 50, 50);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Event QR Code")
                .setView(image)
                .setPositiveButton("Close", null)
                .show();
    }
}