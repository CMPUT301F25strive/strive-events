package com.example.eventlottery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.databinding.FragmentSecondBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreateEventPage extends Fragment {

    private FragmentSecondBinding binding;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private EventRepository eventDataList;
    private Button createEventButton;
    private EditText eventTitleEditText;
    private EditText eventLocationEditText;
    private EditText eventDateEditText;
    private EditText eventVenueEditText;
    private EditText eventCapacityEditText;
    private EditText eventPriceEditText;
    private Switch geoLocationSwitch;
    private EditText eventDescriptionEditText;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventDataList = new EventRepository();

        eventTitleEditText = binding.editEventTitle;
        eventLocationEditText = binding.editLocation;
        eventDateEditText = binding.editDate;
        eventVenueEditText = binding.editVenue;
        eventCapacityEditText = binding.editCapacity;
        eventPriceEditText = binding.editPrice;
        geoLocationSwitch = binding.geoLocationSwitch;
        eventDescriptionEditText = binding.editDescription;
        createEventButton = binding.createEventButton;

        createEventButton.setOnClickListener(v -> {
            int eventID = eventDataList.getSize();
            String eventName = eventTitleEditText.getText().toString();
            String eventDescription = eventDescriptionEditText.getText().toString();
            String eventLocation = eventLocationEditText.getText().toString();
            String eventVenue = eventVenueEditText.getText().toString();
            int eventCapacity = Integer.parseInt(eventCapacityEditText.getText().toString());
            float eventPrice = Float.parseFloat(eventPriceEditText.getText().toString());
            boolean geoRequired = geoLocationSwitch.isChecked();
            Event event = new Event(eventID, eventName, eventDescription, eventVenue, eventCapacity, eventPrice, 1, 0, geoRequired);
            eventDataList.add(event);
        });


        binding.buttonSecond.setOnClickListener(v ->
                NavHostFragment.findNavController(CreateEventPage.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}