package com.example.eventlottery.entrant;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.eventlottery.model.Profile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailFragment extends Fragment {

    public static final String ARG_EVENT = "event";

    private FragmentEventDetailBinding binding;
    private Event currentEvent;

    private boolean isAdmin = false;
    private boolean isOrganizer = false;

    private String deviceId;

    private EventRepository eventRepository;
    private ProfileRepository profileRepository;
    private WaitingListController waitingListController;

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
        isOrganizer = deviceId.equals(event.getOrganizerId());
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
        if (isAdmin || isOrganizer) {
            binding.sendNotificationButton.setVisibility(View.VISIBLE);
            binding.sendNotificationButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_eventDetailFragment_to_sendNotificationFragment)
            );
        } else {
            binding.sendNotificationButton.setVisibility(View.GONE);
        }
    }

    // ------------------ MAP DISPLAY ------------------

    private void displayMapIfEligible() {
        if (currentEvent == null) return;

        // Only show map if geolocation is enabled AND user is admin or organizer
        if (currentEvent.isGeolocationEnabled() && (isAdmin || isOrganizer)) {
            binding.mapContainer.setVisibility(View.VISIBLE);

            SupportMapFragment mapFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    // Fetch user locations from repository
                    eventRepository.getEventUserLocations(currentEvent.getId(), locations -> {
                        for (Event.UserLocation loc : locations) {
                            if (loc.latitude != null && loc.longitude != null) {
                                LatLng pos = new LatLng(loc.latitude, loc.longitude);
                                googleMap.addMarker(new MarkerOptions()
                                        .position(pos)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                        .title("User: " + loc.deviceId));
                            }
                        }

                        // Move camera to first location if available
                        if (!locations.isEmpty()) {
                            LatLng first = new LatLng(locations.get(0).latitude, locations.get(0).longitude);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 12f));
                        }
                    });
                });
            }
        } else {
            binding.mapContainer.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------------------------------------

    private void bindEvent(@NonNull Event event) {
        // Poster
        if (!TextUtils.isEmpty(event.getPosterUrl())) {
            Glide.with(requireContext())
                    .load(event.getPosterUrl())
                    .into(binding.eventDetailPoster);
        } else {
            binding.eventDetailPoster.setImageResource(R.drawable.event_image_placeholder);
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
        if (tag == null) return "Other";

        switch (tag) {
            case ART:        return "Art";
            case MUSIC:      return "Music";
            case EDUCATION:  return "Education";
            case SPORTS:     return "Sports";
            case PARTY:      return "Party";
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
        if (status == null) return "Unknown";

        switch (status) {
            case REG_OPEN:      return "Registration open";
            case REG_CLOSED:    return "Registration closed";
            case DRAWN:         return "Lottery drawn";
            case FINALIZED:     return "Finalized";
            default:            return status.name();
        }
    }

    // ------------------------------------------------------------------------------

    private void setupActionButtons(@NonNull Event event, String userID) {
        binding.buttonContainer.setVisibility(View.VISIBLE);
        setupJoinButton(event, userID);

        if (!event.isRegOpen() || event.isWaitingListFull() || isOrganizer) {
            binding.joinEventButton.setVisibility(View.GONE);
        }

        if (event.isOnWaitingList(userID)
                && (event.isRegOpen() || event.isRegClosed())) {
            binding.joinEventButton.setVisibility(View.VISIBLE);
        }
    }

    private void configAdminButtons(@NonNull Event event){
        boolean canDeleteEvent = isAdmin || isOrganizer;
        binding.adminDeleteButton.setVisibility(canDeleteEvent ? View.VISIBLE : View.GONE);
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
        if (!isAdmin && !isOrganizer) return;

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