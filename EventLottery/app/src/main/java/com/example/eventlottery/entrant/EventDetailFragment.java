package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.FragmentEventDetailBinding;
import com.example.eventlottery.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailFragment extends Fragment {

    public static final String ARG_EVENT = "event";

    private FragmentEventDetailBinding binding;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.eventDetailToolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(EventDetailFragment.this).popBackStack());

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

        bindEvent(event);
    }

    private void bindEvent(@NonNull Event event) {
        binding.eventDetailPoster.setImageResource(event.getPosterResId());
        binding.eventDetailTitle.setText(event.getTitle());
        binding.eventDetailOrganizer.setText(getString(R.string.event_detail_organizer_format, event.getOrganizerName()));

        Date date = new Date(event.getStartTimeMillis());
        String dateCopy = dateFormat.format(date);
        String timeCopy = timeFormat.format(date).toLowerCase(Locale.getDefault());
        binding.eventDetailDate.setText(getString(R.string.event_detail_datetime_format, dateCopy, timeCopy));
        binding.eventDetailVenue.setText(event.getVenue());
        binding.eventDetailCapacity.setText(getString(R.string.event_detail_capacity_format, event.getCapacity()));
        binding.eventDetailSpots.setText(getString(R.string.event_detail_spots_format, Math.max(event.getSpotsRemaining(), 0)));
        if (!TextUtils.isEmpty(event.getDescription())) {
            binding.eventDetailDescription.setText(event.getDescription());
        } else {
            binding.eventDetailDescription.setText(R.string.event_detail_description_placeholder);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Event event = null;
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        }
        if (event != null) {
            outState.putSerializable(ARG_EVENT, event);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
