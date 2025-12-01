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
 * A fragment that allows administrators to edit another user's profile information,
 * including their name, email, phone, role, and organizer status. It also provides
 * functionality to delete the user's profile.
 * <p>
 * This fragment requires a {@code profile_id} to be passed in its arguments bundle
 * to identify the user to be edited.
 * </p>
 * Uses updated XML style: back button + center title only.
 */
public class AdminEditProfileFragment extends Fragment {

    // Key for passing the profile ID as an argument to this fragment.
    public static final String ARG_PROFILE_ID = "profile_id";

    // View binding for the fragment's layout.
    private FragmentEditProfileBinding binding;
    // Repository for accessing and modifying profile data.
    private ProfileRepository profileRepository;
    // The profile object of the user being edited.
    private Profile targetProfile;
    // Array of available roles that can be assigned to a user.
    private Profile.Role[] roleOptions = new Profile.Role[]{
            Profile.Role.USER,
            Profile.Role.ADMIN
    };
    // The currently selected role for the user in the UI.
    private Profile.Role selectedRole = Profile.Role.USER;

    /**
     * Inflates the fragment's view and initializes view binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The root view of the inflated layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the view has been created. Initializes UI components, loads profile data,
     * and sets up event listeners.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileRepository = RepositoryProvider.getProfileRepository();

        // Set up the custom toolbar with a title and back navigation
        setupToolbar();

        // Make the delete button visible and set its click listener
        binding.adminDeleteButton.setVisibility(View.VISIBLE);
        binding.adminDeleteButton.setOnClickListener(v -> confirmDelete());

        // Configure the role selection spinner
        setupRoleSelector();

        // Retrieve the profile ID from fragment arguments
        String profileId = null;
        if (getArguments() != null) {
            profileId = getArguments().getString(ARG_PROFILE_ID);
        }

        // If the profile ID is missing, show an error and navigate back
        if (TextUtils.isEmpty(profileId)) {
            Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        // Load the profile data and set the save button listener
        loadProfile(profileId);
        binding.buttonSave.setOnClickListener(v -> attemptSave());
    }

    /**
     * Sets up the toolbar with a title and a back button.
     */
    private void setupToolbar() {
        binding.backButton.setVisibility(View.VISIBLE);
        binding.appNameText.setText(R.string.admin_profile_edit_title);
        binding.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
    }

    /**
     * Fetches the profile data from the repository using the provided profile ID
     * and populates the UI fields with the retrieved information.
     *
     * @param profileId The unique identifier of the profile to load.
     */
    private void loadProfile(@NonNull String profileId) {
        profileRepository.findUserById(profileId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                if (binding == null) return; // View was destroyed
                targetProfile = profile;

                // Populate UI with profile data
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
            public void onDeleted() {
                // This callback is not expected here.
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Validates the input fields and attempts to save the updated profile information
     * to the repository.
     */
    private void attemptSave() {
        if (targetProfile == null || binding == null) {
            Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Get text from input fields, handling potential nulls
        String name = binding.editTextName.getText() != null
                ? binding.editTextName.getText().toString().trim() : "";
        String email = binding.editTextEmail.getText() != null
                ? binding.editTextEmail.getText().toString().trim() : "";
        String phone = binding.editTextPhone.getText() != null
                ? binding.editTextPhone.getText().toString().trim() : "";

        // Validate required fields
        if (name.isEmpty()) {
            binding.editTextName.setError(getString(R.string.error_field_required));
            return;
        }
        if (email.isEmpty()) {
            binding.editTextEmail.setError(getString(R.string.error_field_required));
            return;
        }

        // Clear any previous errors
        binding.editTextName.setError(null);
        binding.editTextEmail.setError(null);

        // Update the local profile object
        targetProfile.updatePersonalInfo(name, email, phone);
        targetProfile.setRole(selectedRole);
        targetProfile.setOrganizerEnabled(binding.organizerToggle.isChecked());

        // Save the updated profile to the repository
        profileRepository.saveUser(targetProfile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                Toast.makeText(requireContext(), R.string.admin_profile_edit_success, Toast.LENGTH_SHORT).show();
                // Navigate back to the previous screen on success
                NavHostFragment.findNavController(AdminEditProfileFragment.this).popBackStack();
            }

            @Override
            public void onDeleted() {
                // This callback is not expected here.
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initializes the role selection spinner, making it and its related views visible.
     * Sets up an adapter and a listener to handle role selection changes.
     */
    private void setupRoleSelector() {
        // Show admin-specific UI elements
        binding.roleLabel.setVisibility(View.VISIBLE);
        binding.roleSelector.setVisibility(View.VISIBLE);
        binding.organizerToggleLabel.setVisibility(View.VISIBLE);
        binding.organizerToggle.setVisibility(View.VISIBLE);

        // Create and set the adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.profile_role_entries,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSelector.setAdapter(adapter);

        // Set a listener to update the selected role and UI state
        binding.roleSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < roleOptions.length) {
                    selectedRole = roleOptions[position];
                    updateOrganizerToggleState(selectedRole);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    /**
     * Finds the index of a given role within the {@code roleOptions} array.
     *
     * @param role The role to find.
     * @return The index of the role, or 0 if not found.
     */
    private int findRoleIndex(Profile.Role role) {
        for (int i = 0; i < roleOptions.length; i++) {
            if (roleOptions[i] == role) {
                return i;
            }
        }
        return 0; // Default to the first option if not found
    }

    /**
     * Updates the state of the "Organizer" toggle based on the selected role.
     * If the role is ADMIN, the toggle is forced to be checked and disabled.
     * Otherwise, the toggle is enabled for manual control.
     *
     * @param role The currently selected role.
     */
    private void updateOrganizerToggleState(@NonNull Profile.Role role) {
        if (binding == null) return;
        if (role == Profile.Role.ADMIN) {
            binding.organizerToggle.setChecked(true);
            binding.organizerToggle.setEnabled(false);
        } else {
            binding.organizerToggle.setEnabled(true);
        }
    }

    /**
     * Called when the view is destroyed. Cleans up references to the view binding
     * to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Displays a confirmation dialog before proceeding with the profile deletion.
     */
    private void confirmDelete() {
        if (targetProfile == null) {
            Toast.makeText(requireContext(), R.string.admin_profile_edit_error, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_profile_delete_confirm_title)
                .setMessage(R.string.admin_profile_delete_confirm_body)
                .setNegativeButton(android.R.string.cancel, null) // "Cancel" button does nothing
                .setPositiveButton(R.string.admin_profile_actions_delete, (dialog, which) -> performDelete())
                .show();
    }

    /**
     * Executes the deletion of the user profile from the repository.
     * Displays a toast message on success or failure and navigates back.
     */
    private void performDelete() {
        profileRepository.deleteUser(targetProfile.getDeviceID(), new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                // This callback is not expected here.
            }

            @Override
            public void onDeleted() {
                Toast.makeText(requireContext(), R.string.admin_profile_delete_success, Toast.LENGTH_SHORT).show();
                // Navigate back to the previous screen on success
                NavHostFragment.findNavController(AdminEditProfileFragment.this).popBackStack();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), R.string.admin_profile_delete_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
