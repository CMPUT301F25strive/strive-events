package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.FragmentEventListBinding;
import com.example.eventlottery.model.Event;
import com.example.eventlottery.viewmodel.EntrantEventListViewModel;
import com.example.eventlottery.viewmodel.EntrantEventListViewModelFactory;

/**
 * entrant home feed showing joinable events.
 */
public class EntrantEventListFragment extends Fragment implements EventListAdapter.Listener {

    private FragmentEventListBinding binding;
    private EntrantEventListViewModel viewModel;
    private EventListAdapter adapter;
    @Override
    public void onResume() {
        super.onResume();
        binding.bottomNavigation.post(() -> {
            binding.bottomNavigation.setSelectedItemId(-1); // deselect first
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        });
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();
        setupBottomNav();
        binding.pageHeader.setText(R.string.home_page_header);

        viewModel = new ViewModelProvider(this, new EntrantEventListViewModelFactory())
                .get(EntrantEventListViewModel.class);

        binding.eventRefresh.setOnRefreshListener(() -> {
            binding.errorMessage.setVisibility(View.GONE);
            viewModel.refresh();
        });

        viewModel.getState().observe(getViewLifecycleOwner(), this::renderState);

        binding.filterButton.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.filter_content_description, Toast.LENGTH_SHORT).show()
        );
    }

    private void setupRecycler() {
        adapter = new EventListAdapter(this);
        binding.eventRecycler.setAdapter(adapter);
        binding.eventRecycler.setHasFixedSize(true);
    }
    private void setupBottomNav() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
         if (item.getItemId() == R.id.nav_profile) {
             NavHostFragment.findNavController(this)
                     .navigate(R.id.action_entrantEventListFragment_to_profileFragment);
             return true;
         }else if (item.getItemId() == R.id.nav_home) {
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.entrantEventListFragment, false);
                return true;
            } else if (item.getItemId() == R.id.nav_my_events) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.myEventsFragment);
                return true;
            }
            return false;
        });
    }
    private void renderState(@NonNull EventListUiState state) {
        binding.loadingIndicator.setVisibility(state.loading ? View.VISIBLE : View.GONE);
        binding.eventRefresh.setRefreshing(false);
        adapter.submitList(state.events);

        if (state.errorMessage != null) {
            showMessage(state.errorMessage);
        } else if (state.events.isEmpty()) {
            showMessage(getString(R.string.no_events_placeholder));
        } else {
            binding.errorMessage.setVisibility(View.GONE);
        }
    }

    private void showMessage(@NonNull String message) {
        binding.errorMessage.setText(message);
        binding.errorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onEventSelected(@NonNull Event event) {
        Bundle args = new Bundle();
        args.putSerializable(EventDetailFragment.ARG_EVENT, event);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_entrantEventListFragment_to_eventDetailFragment, args);
    }
}
