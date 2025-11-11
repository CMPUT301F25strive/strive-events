package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.databinding.FragmentProfileGuidelinesBinding;

/**
 * Fragment showing the app user guidelines with STRIVE-style header
 */
public class ProfileGuidelinesFragment extends Fragment {

    private FragmentProfileGuidelinesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate using ViewBinding
        binding = FragmentProfileGuidelinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // STRIVE-style back button
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        // Optional: set title dynamically if needed
        binding.appNameText.setText("Guidelines");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}