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
import com.example.eventlottery.databinding.FragmentEditProfileBinding;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);

// Highlight Profile tab
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);

// Bottom nav clicks
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_editProfileFragment_to_entrantEventListFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_editProfileFragment_to_profileFragment);
                return true;
            }
            return true;
        });

        // Save button functional (hard-coded)
        binding.buttonSave.setOnClickListener(v -> {
            String name = binding.editName.getText().toString();
            String email = binding.editEmail.getText().toString();
            String phone = binding.editPhone.getText().toString();

            // For now, just show a toast
            Toast.makeText(requireContext(), "Saved: " + name + ", " + email + ", " + phone, Toast.LENGTH_SHORT).show();

            // Navigate back to ProfileFragment
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_editProfileFragment_to_profileFragment);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}