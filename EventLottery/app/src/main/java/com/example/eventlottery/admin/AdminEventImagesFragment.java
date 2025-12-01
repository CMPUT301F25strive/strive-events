package com.example.eventlottery.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.eventlottery.R;
import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentAdminEventImagesBinding;
import com.example.eventlottery.model.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Simple admin grid listing all event posters with quick delete controls.
 */
public class AdminEventImagesFragment extends Fragment implements AdminEventImagesAdapter.Listener {

    private FragmentAdminEventImagesBinding binding;
    private EventRepository eventRepository;
    private AdminEventImagesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminEventImagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventRepository = RepositoryProvider.getEventRepository();
        adapter = new AdminEventImagesAdapter(this);

        binding.imageGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.imageGrid.setAdapter(adapter);

        binding.backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        eventRepository.observeEvents().observe(getViewLifecycleOwner(), this::bindEvents);
    }

    private void bindEvents(@Nullable List<Event> events) {
        List<Event> safe = events != null ? events : Collections.emptyList();
        List<Event> filtered = new java.util.ArrayList<>();
        for (Event event : safe) {
            if (event != null && event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                filtered.add(event);
            }
        }
        adapter.submitList(filtered);
        binding.emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRemovePoster(@NonNull Event event) {
        if (event.getId() == null) {
            return;
        }
        if (event.getPosterUrl() == null || event.getPosterUrl().isEmpty()) {
            Toast.makeText(requireContext(), R.string.admin_event_images_no_poster, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_event_images_remove_title)
                .setMessage(getString(R.string.admin_event_images_remove_body, event.getTitle()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.admin_event_images_remove_confirm, (dialog, which) -> {
                    eventRepository.removeEventPoster(event.getId());
                    Toast.makeText(requireContext(), R.string.admin_event_images_remove_success, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
