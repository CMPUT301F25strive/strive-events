package com.example.eventlottery.entrant;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.WaitingListController;
import com.example.eventlottery.admin.AdminGate;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentEventDetailBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.model.InvitationService;
import com.example.eventlottery.model.Profile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailFragment extends Fragment {

    public static final String ARG_EVENT = "event";

    private FragmentEventDetailBinding binding;
    private Event currentEvent;

    private boolean isAdmin = false;
    private boolean isOwner = false;

    private String deviceId;

    private EventRepository eventRepository;
    private ProfileRepository profileRepository;
    private WaitingListController waitingListController;
    private InvitationService invitationService;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("h:mm a", Locale.getDefault());

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // ------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // ------------------------------------------------------------------------------

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        eventRepository = RepositoryProvider.getEventRepository();
        profileRepository = RepositoryProvider.getProfileRepository();
        waitingListController = new WaitingListController(eventRepository, profileRepository);
        invitationService = new InvitationService();
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Retrieve event ------------------------------------------------------------------
        Event event = null;
        if (savedInstanceState != null) {
            event = (Event) savedInstanceState.getSerializable(ARG_EVENT);
        }
        if (event == null && getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        }
        if (event == null) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        currentEvent = event;

        // Set flags FIRST ------------------------------------------------------------------
        isOwner = deviceId.equals(event.getOrganizerId());
        isAdmin = AdminGate.isAdmin(requireContext());

        // Re-check admin using profile data (updates if needed)
        profileRepository.findUserById(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                boolean repoAdmin = profile != null && profile.isAdmin();
                if (repoAdmin != isAdmin) {
                    isAdmin = repoAdmin;
                }

                // Check for map display AFTER admin/organizer flags are determined
                displayMapIfEligible();
            }

            @Override public void onDeleted() {}
            @Override public void onError(String message) {}
        });

        bindEvent(event);
        setupActionButtons(event, deviceId);
        setupRsvpUI();
        configAdminButtons(event);

        final String eventId = event.getId();

        eventRepository.observeEvents().observe(getViewLifecycleOwner(), updatedList -> {
            Event updated = eventRepository.findEventById(eventId);
            if (updated != null) {
                currentEvent = updated;
                bindEvent(updated);
                setupActionButtons(updated, deviceId);
                configAdminButtons(updated);
            }
        });
        if (currentEvent != null && currentEvent.isGeolocationEnabled()) {
            Toast.makeText(requireContext(), "Geolocation is required for this event", Toast.LENGTH_SHORT).show();
        }
        // send notification button in the event details screen
        if (isAdmin || isOwner) {
            binding.sendNotificationButton.setVisibility(View.VISIBLE);
            binding.sendNotificationButton.setOnClickListener(v -> {
                if (currentEvent == null) return;

                Bundle bundle = new Bundle(); // sending the event to the sendNotificationFragment to know which event it is dealing with
                bundle.putSerializable("currentEvent", currentEvent);

                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_eventDetailFragment_to_sendNotificationFragment, bundle);
            });
            // Generate QR Code
            binding.generateQRButton.setVisibility(View.VISIBLE);
            binding.generateQRButton.setOnClickListener(v ->{generateQRCode(eventId);});
        } else {
            binding.sendNotificationButton.setVisibility(View.GONE);
            binding.generateQRButton.setVisibility(View.GONE);
        }
    }


    // ------------------ MAP DISPLAY ------------------

    private void displayMapIfEligible() {
        if (currentEvent == null) return;

        // Only show map if geolocation is enabled AND user is admin or organizer
        if (currentEvent.isGeolocationEnabled() && (isAdmin || isOwner)) {
            binding.mapContainer.setVisibility(View.VISIBLE);

            SupportMapFragment mapFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        // Enable zoom controls
                        googleMap.getUiSettings().setZoomControlsEnabled(true);

                        // Optional: Enable pinch-to-zoom gestures
                        googleMap.getUiSettings().setZoomGesturesEnabled(true);
                    }
                });
            }

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    eventRepository.getEventUserLocations(currentEvent.getId(), locations -> {
                        if (locations.isEmpty()) return;

                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                        for (Event.UserLocation loc : locations) {
                            if (loc.latitude != null && loc.longitude != null) {
                                LatLng pos = new LatLng(loc.latitude, loc.longitude);
                                googleMap.addMarker(new MarkerOptions()
                                        .position(pos)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                        .title("User: " + loc.deviceId));

                                boundsBuilder.include(pos);
                            }
                        }

                        // Move camera to include all markers
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200));
                    });
                });
            }
        } else {
            binding.mapContainer.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------------------------------------

    private void bindEvent(@NonNull Event event) {
        if (!TextUtils.isEmpty(event.getPosterUrl())) {
            binding.posterWatermark.setVisibility(View.GONE);
            Glide.with(requireContext())
                    .load(event.getPosterUrl())
                    .into(binding.eventDetailPoster);
        } else {
            binding.eventDetailPoster.setImageResource(R.drawable.event_image_placeholder);
            binding.posterWatermark.setVisibility(View.VISIBLE);
        }

        binding.eventDetailTitle.setText(event.getTitle());
        fetchAndDisplayOrganizerName(event.getOrganizerId());

        // Event date/time
        Date date = new Date(event.getEventStartTimeMillis());
        String dateStr = dateFormat.format(date);
        String timeStr = timeFormat.format(date)
                .toLowerCase(Locale.getDefault());

        binding.eventDetailDate.setText(
                getString(R.string.event_detail_datetime_format, dateStr, timeStr)
        );

        // Venue (Location)
        binding.eventDetailVenue.setText(event.getVenue());

        // Capacity
        binding.eventDetailCapacity.setText(
                getString(R.string.event_detail_capacity_format, event.getCapacity())
        );

        // Waiting list count
        binding.eventDetailWaitingListCount.setText(
                getString(R.string.event_detail_waiting_list_count_format, event.getWaitingListSize())
        );

        // Total waiting list spots ( -1 == unlimited)
        int waitingListSpots = event.getWaitingListSpots();
        if (waitingListSpots < 0) {
            binding.eventDetailWaitingListSpots.setText(
                    getString(R.string.event_detail_waiting_list_spots_unlimited_format)
            );
        } else {
            binding.eventDetailWaitingListSpots.setText(
                    getString(R.string.event_detail_waiting_list_spots_format, waitingListSpots)
            );
        }

        // Registration period (reg start & reg end)
        long regStartMillis = event.getRegStartTimeMillis();
        long regEndMillis   = event.getRegEndTimeMillis();
        Date regStartDate = new Date(regStartMillis);
        Date regEndDate   = new Date(regEndMillis);

        // Parse the date and time
        String regStartStr = dateFormat.format(regStartDate) + " · " +
                timeFormat.format(regStartDate).toLowerCase(Locale.getDefault());
        String regEndStr = dateFormat.format(regEndDate) + " · " +
                timeFormat.format(regEndDate).toLowerCase(Locale.getDefault());

        binding.eventDetailRegPeriod.setText(
                getString(R.string.event_detail_reg_period_format, regStartStr, regEndStr)
        );

        // Tag
        String tagDisplay = getDisplayTag(event.getTag());
        binding.eventDetailTag.setText(
                getString(R.string.event_detail_tag_format, tagDisplay)
        );

        // Status
        String statusDisplay = getDisplayStatus(event);
        binding.eventDetailStatus.setText(
                getString(R.string.event_detail_status_format, statusDisplay)
        );

        // Description
        binding.eventDetailDescription.setText(
                getString(R.string.event_detail_description_placeholder, event.getDescription())
        );
    }

    // ------------------------------------------------------------------------------

    /**
     * This method renders the tag of event from Event.Tag enum identifier
     * to its meaning as a String.
     * @param tag: the tag identifier of Event.Tag
     * @return the corresponding String format
     */
    private String getDisplayTag(@Nullable Event.Tag tag) {
        // If tag is missing
        if (tag == null) return "Other";

        // Render the given tag
        switch (tag) {
            case ART:        return "Art";
            case MUSIC:      return "Music";
            case EDUCATION:  return "Education";
            case SPORTS:     return "Sports";
            case PARTY:      return "Party";
            // If mismatched, return the exact enum constant name as a String
            default:         return tag.name();
        }
    }

    /**
     * This method renders the status of event from Event.Status enum identifier
     * to its corresponding meaning as a String.
     * @param event: the status identifier of Event.Status
     * @return the corresponding String format
     */
    private String getDisplayStatus(@NonNull Event event) {
        Event.Status status = event.getStatus();
        // If status is missing
        if (status == null) return "Unknown";

        // Render the given status
        switch (status) {
            case REG_OPEN:      return "Registration open";
            case REG_CLOSED:    return "Registration closed";
            case DRAWN:         return "Lottery drawn";
            case FINALIZED:     return "Finalized";
            // If mismatched, return the exact enum constant name as a String
            default:            return status.name();
        }
    }

    // ------------------------------------------------------------------------------

    private void setupActionButtons(@NonNull Event event, String userID) {
        binding.buttonContainer.setVisibility(View.VISIBLE);
        setupJoinButton(event, userID);

        // Cannot join if the event is out of registration period, the waiting list is full, or it's their own event
        if (!event.isRegOpen() || event.isWaitingListFull() || isOwner) {
            binding.joinEventButton.setVisibility(View.GONE);
        }

        // If already in the waiting list, we can always leave before status.DRAWN
        if (event.isOnWaitingList(userID)
                && (event.isRegOpen() || event.isRegClosed())) {
            binding.joinEventButton.setVisibility(View.VISIBLE);
        }
    }

    private void configAdminButtons(@NonNull Event event){
        // Delete: admin or event owner
        boolean canDeleteEvent = isAdmin || isOwner;
        binding.adminDeleteButton.setVisibility(canDeleteEvent ? View.VISIBLE : View.GONE);

        // Only the owner of the event can view the waiting list
        binding.viewWaitingListButton.setVisibility(
                isOwner ? View.VISIBLE : View.GONE
        );

        if (isOwner) {
            binding.viewWaitingListButton.setOnClickListener(v -> {
                if (currentEvent == null) return;

                Bundle bundle = new Bundle();
                bundle.putString(WaitingListFragment.ARG_EVENT_ID, currentEvent.getId());

                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_eventDetailFragment_to_waitingListFragment, bundle);
            });
        } else {
            // If not the owner, clear the listener
            binding.viewWaitingListButton.setOnClickListener(null);
        }

        if (canDeleteEvent) {
            binding.adminDeleteButton.setOnClickListener(v -> {
                if (currentEvent == null) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_event_confirm_title)
                        .setMessage(R.string.delete_event_confirm_body)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.delete_event, (dialog, which) -> performDelete())
                        .show();
            });
        }

        boolean canRemovePoster = isAdmin && !TextUtils.isEmpty(event.getPosterUrl());
        binding.posterRemoveButton.setVisibility(canRemovePoster ? View.VISIBLE : View.GONE);
        if (canRemovePoster) {
            binding.posterRemoveButton.setOnClickListener(v -> confirmPosterRemoval());
        } else {
            binding.posterRemoveButton.setOnClickListener(null);
        }
    }

    private void setupJoinButton(@NonNull Event event, String userID) {
        final MaterialButton joinButton = binding.joinEventButton;

        updateJoinButton(event.isOnWaitingList(userID));

        joinButton.setOnClickListener(v -> {
            boolean onWait = event.isOnWaitingList(userID);

            if (onWait) {
                if (event.isGeolocationEnabled()) {
                    removeLocationAndLeave(event, userID);
                } else {
                    waitingListController.leaveWaitingList(event.getId(), userID);
                    Toast.makeText(requireContext(), "Left waiting list", Toast.LENGTH_SHORT).show();
                    updateJoinButton(false);
                }
            } else {
                if (event.getStatus() != Event.Status.REG_OPEN) {
                    Toast.makeText(requireContext(), "Registration closed", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (event.isGeolocationEnabled()) {
                    checkLocationPermissionAndJoin(event, userID);
                } else {
                    waitingListController.joinWaitingList(event.getId(), userID);
                    Toast.makeText(requireContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
                    updateJoinButton(true);
                }
            }
        });
    }

    private void updateJoinButton(boolean onList) {
        if (binding == null) return;

        MaterialButton btn = binding.joinEventButton;

        if (onList) {
            btn.setText(R.string.leave_waiting_list);
            btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error));
            btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        } else {
            btn.setText(R.string.join_waiting_list);
            btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary));
            btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        }
    }

    // ------------------------------------------------------------------------------

    private void performDelete() {
        if (!isAdmin && !isOwner) return;

        try {
            eventRepository.deleteEvent(currentEvent.getId());
            Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();

            NavHostFragment.findNavController(this).popBackStack();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmPosterRemoval() {
        if (!isAdmin || currentEvent == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_poster_delete_confirm_title)
                .setMessage(R.string.admin_poster_delete_confirm_body)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.admin_poster_delete_button, (dialog, which) -> performPosterRemoval())
                .show();
    }

    private void performPosterRemoval() {
        if (currentEvent == null) return;
        try {
            eventRepository.removeEventPoster(currentEvent.getId());
            binding.eventDetailPoster.setImageResource(R.drawable.event_image_placeholder);
            Toast.makeText(requireContext(), R.string.admin_poster_delete_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.admin_poster_delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------------------------------------

    private void fetchAndDisplayOrganizerName(String organizerId) {
        if (organizerId == null || organizerId.isEmpty()) {
            binding.eventDetailOrganizer.setText("Organizer: Unknown");
            return;
        }

        profileRepository.findUserById(organizerId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                if (binding == null) return;

                if (profile != null) {
                    binding.eventDetailOrganizer.setText(
                            "Organizer: " + profile.getName().toUpperCase()
                    );
                }
            }

            @Override
            public void onDeleted() {
                if (binding != null)
                    binding.eventDetailOrganizer.setText("Organizer: Deleted User");
            }

            @Override
            public void onError(String msg) {
                if (binding != null)
                    binding.eventDetailOrganizer.setText("Organizer: UNKNOWN");
            }
        });
    }

    // ------------------------------------------------------------------------------

    // ------------------ GEOLOCATION HANDLING ------------------

    private void checkLocationPermissionAndJoin(Event event, String userID) {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            getDeviceLocationAndJoin(event, userID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (currentEvent != null) {
                    getDeviceLocationAndJoin(currentEvent, deviceId);
                }
            } else {
                Toast.makeText(requireContext(), "Location permission is required to join waiting list.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getDeviceLocationAndJoin(Event event, String userID) {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Wrap lat/lon in a Location object
                            Location loc = new Location("");
                            loc.setLatitude(location.getLatitude());
                            loc.setLongitude(location.getLongitude());

                            waitingListController.joinWaitingList(event.getId(), userID, loc);
                        } else {
                            waitingListController.joinWaitingList(event.getId(), userID);
                        }
                        Toast.makeText(requireContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
                        updateJoinButton(true);
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void removeLocationAndLeave(Event event, String userID) {
        if (event.getUserLocations() != null) {
            event.getUserLocations().removeIf(loc -> loc.deviceId.equals(userID));
        }

        waitingListController.leaveWaitingList(event.getId(), userID);
        Toast.makeText(requireContext(), "Left waiting list", Toast.LENGTH_SHORT).show();
        updateJoinButton(false);
    }

    // ------------------------------------------------------------------------------
    // ------------------ QR CODE HANDLING ------------------
    /**
     * Generate QR code for the event
     * @param eventID: the event ID to use to generate the QR code
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

    /**
     * This method sets the UI for RSVP with proper visibility and corresponding listeners.
     */
    private void setupRsvpUI() {
        if (currentEvent == null) {
            binding.layoutRsvp.setVisibility(View.GONE);
            return;
        }

        // The event owner should not see RSVP UI
        boolean isOwner = deviceId != null && deviceId.equals(currentEvent.getOrganizerId());
        if (isOwner) {
            binding.layoutRsvp.setVisibility(View.GONE);
            return;
        }

        // Entrant lists
        List<String> invited = currentEvent.getInvitedList();
        List<String> attendees = currentEvent.getAttendeesList();
        List<String> canceled = currentEvent.getCanceledList();

        // Check conditions before the real RSVP action
        boolean isInvited = invited != null && invited.contains(deviceId);
        boolean isAttending = attendees != null && attendees.contains(deviceId);
        boolean isCanceled = canceled != null  && canceled.contains(deviceId);

        // Can RSVP only when user is invited and hasn't responded to that
        boolean canRsvp = isInvited
                && !isAttending
                && !isCanceled
                && currentEvent.isRegEnd()
                && !currentEvent.isEventStarted();

        if (!canRsvp) {
            // If cannot RSVP, hide the RSVP UI
            binding.layoutRsvp.setVisibility(View.GONE);
            return;
        }

        // When user can RSVP:
        binding.layoutRsvp.setVisibility(View.VISIBLE);

        binding.buttonAccept.setOnClickListener(v -> {
            handleAccept();
        });
        binding.buttonDecline.setOnClickListener(v -> {
            handleDecline();
        });
    }

    /**
     * This method handles the situation when the invited entrant accepts the invitation.
     */
    private void handleAccept() {
        if (currentEvent == null || deviceId == null) return;

        binding.buttonAccept.setEnabled(false);
        binding.buttonDecline.setEnabled(false);

        invitationService.acceptInvitation(deviceId, currentEvent.getId());

        Toast.makeText(requireContext(), "You have accepted this invitation.", Toast.LENGTH_SHORT).show();

        // Hide the whole RSVP UI after responding
        binding.layoutRsvp.setVisibility(View.GONE);
    }

    /**
     * This method handles the situation when the invited entrant accepts the invitation.
     */
    private void handleDecline() {
        if (currentEvent == null || deviceId == null) return;

        binding.buttonAccept.setEnabled(false);
        binding.buttonDecline.setEnabled(false);

        invitationService.declineInvitation(deviceId, currentEvent.getId());

        Toast.makeText(requireContext(), "You have declined this invitation.", Toast.LENGTH_SHORT).show();

        // Hide the whole RSVP UI after responding
        binding.layoutRsvp.setVisibility(View.GONE);
    }
    // ------------------------------------------------------
    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (currentEvent != null)
            out.putSerializable(ARG_EVENT, currentEvent);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}