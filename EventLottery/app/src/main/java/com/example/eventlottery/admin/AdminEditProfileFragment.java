package com.example.eventlottery.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 */
public class AdminEditProfileFragment extends Fragment {

    public static final String ARG_PROFILE_ID = "profile_id";

    private FragmentEditProfileBinding binding;
    private ProfileRepository profileRepository;
    private Profile targetProfile;

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

        binding.editProfileToolbar.setTitle(R.string.admin_profile_edit_title);
        binding.editProfileToolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
        binding.adminDeleteButton.setVisibility(View.VISIBLE);
        binding.adminDeleteButton.setOnClickListener(v -> confirmDelete());

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
                if (binding == null) {
                    return;
                }
                targetProfile = profile;
                binding.editTextName.setText(profile.getName());
                binding.editTextEmail.setText(profile.getEmail());
                binding.editTextPhone.setText(profile.getPhone());
            }

            @Override
            public void onDeleted() {
            }

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
        profileRepository.saveUser(targetProfile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                Toast.makeText(requireContext(), R.string.admin_profile_edit_success, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(AdminEditProfileFragment.this).popBackStack();
            }

            @Override
            public void onDeleted() {
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            }
        });
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
            public void onSuccess(Profile profile) {
            }

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
