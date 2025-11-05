package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // highlight current tab
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);

        // bottom nav clicks
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_entrantEventListFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_my_events) {
                Toast.makeText(requireContext(), R.string.nav_my_events, Toast.LENGTH_SHORT).show();
                binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
                return false;
            }
            return true;
        });

        // Edit Profile button functional
        binding.buttonEditProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileFragment_to_editProfileFragment)
        );

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}