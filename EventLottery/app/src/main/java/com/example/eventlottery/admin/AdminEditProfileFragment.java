package com.example.eventlottery.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.databinding.FragmentEditProfileBinding;
import com.example.eventlottery.model.Profile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Allows administrators to edit another entrant's profile information.
 * Uses updated XML style: back button + center title only.
 */
public class AdminEditProfileFragment extends Fragment {

    public static final String ARG_PROFILE_ID = "profile_id";

    private FragmentEditProfileBinding binding;
    private ProfileRepository profileRepository;
    private Profile targetProfile;
    private Profile.Role[] roleOptions = new Profile.Role[]{
            Profile.Role.USER,
            Profile.Role.ADMIN
    };
    private Profile.Role selectedRole = Profile.Role.USER;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileRepository = RepositoryProvider.getProfileRepository();

        // Set toolbar title and back navigation
        binding.backButton.setVisibility(View.VISIBLE);
        binding.appNameText.setText(R.string.admin_profile_edit_title);
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        binding.adminDeleteButton.setVisibility(View.VISIBLE);
        binding.adminDeleteButton.setOnClickListener(v -> confirmDelete());

        setupRoleSelector();

        String profileId = null;
        if (getArguments() != null) {
            profileId = getArguments().getString(ARG_PROFILE_ID);
        }
        if (TextUtils.isEmpty(profileId)) {
            Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        loadProfile(profileId);
        binding.buttonSave.setOnClickListener(v -> attemptSave());
    }

    private void loadProfile(@NonNull String profileId) {
        profileRepository.findUserById(profileId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                if (binding == null) return;
                targetProfile = profile;
                binding.editTextName.setText(profile.getName());
                binding.editTextEmail.setText(profile.getEmail());
                binding.editTextPhone.setText(profile.getPhone());
                selectedRole = profile.getRole();
                int index = findRoleIndex(selectedRole);
                if (index >= 0) {
                    binding.roleSelector.setSelection(index);
                }
                binding.organizerToggle.setChecked(profile.isOrganizerEnabled());
                updateOrganizerToggleState(selectedRole);
            }

            @Override
            public void onDeleted() { }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptSave() {
        if (targetProfile == null || binding == null) {
            Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String name = binding.editTextName.getText() != null
                ? binding.editTextName.getText().toString().trim() : "";
        String email = binding.editTextEmail.getText() != null
                ? binding.editTextEmail.getText().toString().trim() : "";
        String phone = binding.editTextPhone.getText() != null
                ? binding.editTextPhone.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.editTextName.setError(getString(R.string.error_field_required));
            return;
        }
        if (email.isEmpty()) {
            binding.editTextEmail.setError(getString(R.string.error_field_required));
            return;
        }

        binding.editTextName.setError(null);
        binding.editTextEmail.setError(null);

        targetProfile.updatePersonalInfo(name, email, phone);
        targetProfile.setRole(selectedRole);
        targetProfile.setOrganizerEnabled(binding.organizerToggle.isChecked());
        profileRepository.saveUser(targetProfile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                Toast.makeText(requireContext(), R.string.admin_profile_edit_success, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(AdminEditProfileFragment.this).popBackStack();
            }

            @Override
            public void onDeleted() { }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRoleSelector() {
        binding.roleLabel.setVisibility(View.VISIBLE);
        binding.roleSelector.setVisibility(View.VISIBLE);
        binding.organizerToggleLabel.setVisibility(View.VISIBLE);
        binding.organizerToggle.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.profile_role_entries,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSelector.setAdapter(adapter);
        binding.roleSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < roleOptions.length) {
                    selectedRole = roleOptions[position];
                    updateOrganizerToggleState(selectedRole);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private int findRoleIndex(Profile.Role role) {
        for (int i = 0; i < roleOptions.length; i++) {
            if (roleOptions[i] == role) {
                return i;
            }
        }
        return 0;
    }

    private void updateOrganizerToggleState(@NonNull Profile.Role role) {
        if (binding == null) return;
        if (role == Profile.Role.ADMIN) {
            binding.organizerToggle.setChecked(true);
            binding.organizerToggle.setEnabled(false);
        } else {
            binding.organizerToggle.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void confirmDelete() {
        if (targetProfile == null) {
            Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_profile_delete_confirm_title)
                .setMessage(R.string.admin_profile_delete_confirm_body)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.admin_profile_actions_delete, (dialog, which) -> performDelete())
                .show();
    }

    private void performDelete() {
        profileRepository.deleteUser(targetProfile.getDeviceID(), new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) { }

            @Override
            public void onDeleted() {
                Toast.makeText(requireContext(), R.string.admin_profile_delete_success, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(AdminEditProfileFragment.this).popBackStack();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), R.string.admin_profile_delete_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
