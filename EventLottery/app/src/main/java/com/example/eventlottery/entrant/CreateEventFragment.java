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
import android.widget.Switch;
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

    // Request code for opening the gallery
    private static final int PICK_IMAGE = 10;

    // Views and data holders
    private ImageView eventPoster;
    private Uri imageUri; // Holds the URI of selected poster image

    private TextInputEditText editTextTitle, editTextDescription, editTextLocation,
            editTextEventDate, editTextEventTime,
            editTextRegStartDate, editTextRegStartTime,
            editTextRegEndDate, editTextRegEndTime,
            editTextCapacity, editTextWaitingListSpots;
    private TextView charCountText;
    private Switch switchGeolocation;
    private Button saveButton;
    private ImageButton backButton;
    private ChipGroup tagChipGroup;
    private Event.Tag selectedTag = null; // Holds currently selected tag

    private FirebaseEventRepository firebaseRepo; // Repository to save events to Firebase

    // Calendar instances for event & registration timings
    private final Calendar eventCalendar = Calendar.getInstance();
    private final Calendar regStartCalendar = Calendar.getInstance();
    private final Calendar regEndCalendar = Calendar.getInstance();

    public CreateEventFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Security check: Only organizers can access this screen
        if (!OrganizerGate.hasOrganizerAccess(requireContext())) {
            Toast.makeText(requireContext(), R.string.organizer_feature_required, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack(); // Go back if unauthorized
            return;
        }

        firebaseRepo = new FirebaseEventRepository(); // Initialize Firebase repo

        initViews(view);         // Link views
        setupTagSelection();     // Setup category tag selection
        setupListeners();        // Setup button, text, and date/time listeners
    }

    /**
     * Initialize all the views by binding them with their IDs
     */
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
        switchGeolocation = view.findViewById(R.id.switchGeolocation);
    }

    /**
     * Setup the chip group for selecting event category/tag
     * Only one tag can be selected at a time
     */
    private void setupTagSelection() {
        tagChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedTag = null; // No tag selected
                return;
            }

            int id = checkedIds.get(0); // Take the first selected chip

            // Map chip IDs to Event.Tag enum
            if (id == R.id.chip_art) selectedTag = Event.Tag.ART;
            else if (id == R.id.chip_music) selectedTag = Event.Tag.MUSIC;
            else if (id == R.id.chip_education) selectedTag = Event.Tag.EDUCATION;
            else if (id == R.id.chip_sports) selectedTag = Event.Tag.SPORTS;
            else if (id == R.id.chip_party) selectedTag = Event.Tag.PARTY;
            else selectedTag = null;
        });
    }

    /**
     * Setup click listeners and text change listeners
     */
    private void setupListeners() {
        // Open gallery to pick poster
        eventPoster.setOnClickListener(v -> openGallery());

        // Back button pressed
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // Live character count for description field
        editTextDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCountText.setText(s.length() + " / 200");
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Event date/time pickers
        editTextEventDate.setOnClickListener(v -> showDatePicker(editTextEventDate, editTextEventTime, eventCalendar));
        editTextEventTime.setOnClickListener(v -> showTimePicker(editTextEventTime, editTextEventDate, eventCalendar));

        // Registration start/end date/time pickers
        editTextRegStartDate.setOnClickListener(v -> showDatePicker(editTextRegStartDate, editTextRegStartTime, regStartCalendar));
        editTextRegStartTime.setOnClickListener(v -> showTimePicker(editTextRegStartTime, editTextRegStartDate, regStartCalendar));
        editTextRegEndDate.setOnClickListener(v -> showDatePicker(editTextRegEndDate, editTextRegEndTime, regEndCalendar));
        editTextRegEndTime.setOnClickListener(v -> showTimePicker(editTextRegEndTime, editTextRegEndDate, regEndCalendar));

        // Save event button
        saveButton.setOnClickListener(v -> saveEvent(v));
    }

    /**
     * Open the device gallery to select an image
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    /**
     * Receive the selected image URI from gallery
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();          // Save selected image URI
            eventPoster.setImageURI(imageUri);  // Display selected image
        }
    }

    /**
     * Generic date picker for both event and registration dates
     */
    /**
     * Generic date picker for both event and registration dates
     */
    private void showDatePicker(TextInputEditText targetDateField,
                                TextInputEditText relatedTimeField,
                                @Nullable Calendar targetCalendar) {
        Calendar now = Calendar.getInstance();

        // Default min date logic: if this is the EVENT date field, set min = now + 3 days
        long minDateMillis = now.getTimeInMillis(); // default for registration
        if (targetDateField == editTextEventDate) {
            Calendar minEventDate = Calendar.getInstance();
            minEventDate.add(Calendar.DAY_OF_YEAR, 3); // Event must start at least 3 days from now
            minDateMillis = minEventDate.getTimeInMillis();
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    if (targetCalendar != null) {
                        targetCalendar.set(Calendar.YEAR, year);
                        targetCalendar.set(Calendar.MONTH, month);
                        targetCalendar.set(Calendar.DAY_OF_MONTH, day);
                    }

                    String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                    targetDateField.setText(formatted);

                    // If time not set yet, set default 00:00
                    if (relatedTimeField != null && relatedTimeField.getText() != null &&
                            relatedTimeField.getText().toString().trim().isEmpty()) {
                        if (targetCalendar != null) {
                            targetCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            targetCalendar.set(Calendar.MINUTE, 0);
                            targetCalendar.set(Calendar.SECOND, 0);
                            targetCalendar.set(Calendar.MILLISECOND, 0);
                        }
                        relatedTimeField.setText("00:00");
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.getDatePicker().setMinDate(minDateMillis); // Disable past dates or less than 3 days for event
        datePicker.show();
    }
    /**
     * Generic time picker for event & registration times
     */
    private void showTimePicker(TextInputEditText targetTimeField,
                                TextInputEditText relatedDateField,
                                @Nullable Calendar targetCalendar) {

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        Calendar selectedDate = Calendar.getInstance();
        String dateText = relatedDateField.getText() != null ? relatedDateField.getText().toString().trim() : "";

        // Parse selected date
        if (!dateText.isEmpty()) {
            String[] parts = dateText.split("/");
            if (parts.length == 3) {
                try {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1;
                    int year = Integer.parseInt(parts[2]);
                    selectedDate.set(year, month, day);
                } catch (NumberFormatException ignored) {
                    selectedDate.setTime(now.getTime());
                }
            } else selectedDate.setTime(now.getTime());
        } else selectedDate.setTime(now.getTime());

        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    // Prevent selecting past time if today
                    boolean isToday = selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
                    if (isToday && (selectedHour < currentHour ||
                            (selectedHour == currentHour && selectedMinute < currentMinute))) {
                        Toast.makeText(requireContext(), "Cannot select past time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (targetCalendar != null) {
                        targetCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        targetCalendar.set(Calendar.MINUTE, selectedMinute);
                        targetCalendar.set(Calendar.SECOND, 0);
                        targetCalendar.set(Calendar.MILLISECOND, 0);
                    }

                    String formatted = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    targetTimeField.setText(formatted);

                    // If date not set, default to today
                    if (relatedDateField != null && relatedDateField.getText() != null &&
                            relatedDateField.getText().toString().trim().isEmpty()) {
                        int year = now.get(Calendar.YEAR);
                        int month = now.get(Calendar.MONTH);
                        int day = now.get(Calendar.DAY_OF_MONTH);
                        if (targetCalendar != null) {
                            targetCalendar.set(Calendar.YEAR, year);
                            targetCalendar.set(Calendar.MONTH, month);
                            targetCalendar.set(Calendar.DAY_OF_MONTH, day);
                        }
                        formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                        relatedDateField.setText(formatted);
                    }
                },
                currentHour, currentMinute,
                false // 12h format
        );

        timePicker.show();
    }

    /**
     * Collect input, validate, and save the event to Firebase
     */
    private void saveEvent(View root) {
        // Basic required fields
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

        long now = System.currentTimeMillis();

        // If eventCalendar wasn't set (no date/time picked), default to now
        if (editTextEventDate.getText().toString().trim().isEmpty() ||
                editTextEventTime.getText().toString().trim().isEmpty()) {
            eventCalendar.setTimeInMillis(now);
            editTextEventDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    eventCalendar.get(Calendar.DAY_OF_MONTH),
                    eventCalendar.get(Calendar.MONTH) + 1,
                    eventCalendar.get(Calendar.YEAR)));
            editTextEventTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    eventCalendar.get(Calendar.HOUR_OF_DAY),
                    eventCalendar.get(Calendar.MINUTE)));
        }

        long eventStartTime = eventCalendar.getTimeInMillis();

        // Registration start default → now if empty
        if (editTextRegStartDate.getText().toString().trim().isEmpty() ||
                editTextRegStartTime.getText().toString().trim().isEmpty()) {
            regStartCalendar.setTimeInMillis(now);
            editTextRegStartDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    regStartCalendar.get(Calendar.DAY_OF_MONTH),
                    regStartCalendar.get(Calendar.MONTH) + 1,
                    regStartCalendar.get(Calendar.YEAR)));
            editTextRegStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    regStartCalendar.get(Calendar.HOUR_OF_DAY),
                    regStartCalendar.get(Calendar.MINUTE)));
        }

        long regStart = regStartCalendar.getTimeInMillis();

        // Registration end default → 24 hours before event start if empty
        if (editTextRegEndDate.getText().toString().trim().isEmpty() ||
                editTextRegEndTime.getText().toString().trim().isEmpty()) {
            regEndCalendar.setTimeInMillis(eventStartTime - 24 * 60 * 60 * 1000); // 24h before
            editTextRegEndDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    regEndCalendar.get(Calendar.DAY_OF_MONTH),
                    regEndCalendar.get(Calendar.MONTH) + 1,
                    regEndCalendar.get(Calendar.YEAR)));
            editTextRegEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    regEndCalendar.get(Calendar.HOUR_OF_DAY),
                    regEndCalendar.get(Calendar.MINUTE)));
        }

        long regEnd = regEndCalendar.getTimeInMillis();

        // Event must start MIN_EVENT_LEAD_DAYS from today
        if (!EventTimeValidator.isEventDateValid(Calendar.getInstance(), eventCalendar)) {
            Toast.makeText(requireContext(),
                    "Event must start at least " + EventTimeValidator.MIN_EVENT_LEAD_DAYS + " days from today.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Adjust registration start if somehow in past
        if (regStart < now) regStart = now;

        if (regEnd <= regStart || eventStartTime - regEnd < EventTimeValidator.MIN_REG_DRAWN_GAP_MILLIS) {
            Toast.makeText(requireContext(),
                    "Invalid registration period. Registration must close at least 24h before event starts.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Parse capacity & waiting list as before
        int capacity;
        try { capacity = Integer.parseInt(editTextCapacity.getText().toString().trim()); }
        catch (NumberFormatException e) { Toast.makeText(requireContext(), "Invalid capacity", Toast.LENGTH_SHORT).show(); return; }

        int waitingList = -1;
        String waitingListStr = editTextWaitingListSpots.getText().toString().trim();
        if (!waitingListStr.isEmpty()) {
            try { waitingList = Integer.parseInt(waitingListStr); }
            catch (NumberFormatException e) { Toast.makeText(requireContext(), "Invalid waiting list spots", Toast.LENGTH_SHORT).show(); return; }
        }

        boolean geolocationEnabled = switchGeolocation.isChecked();
        // Disable button during upload
        saveButton.setEnabled(false);
        saveButton.setText("Posting...");

        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Upload event to Firebase
        firebaseRepo.uploadEvent(imageUri, title, description, location, eventStartTime,
                regStart, regEnd, capacity, waitingList, deviceId, selectedTag,
                geolocationEnabled, new FirebaseEventRepository.UploadCallback() {
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
                            Navigation.findNavController(root).navigateUp(); // Go back to event list
                            generateQRCode(eventID); // Show QR for the event
                        }
                    }
                });
    }

    /**
     * Generate QR code for the event
     */
    private void generateQRCode(String eventID) {
        try {
            String qrLink = "event/" + eventID;
            int size = 600; // QR size
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrLink, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            showQrDialog(bitmap); // Display the QR code
        } catch (Exception e) {
            Toast.makeText(requireContext(), "QR generation failed: " + e, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show QR code in a dialog
     */
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