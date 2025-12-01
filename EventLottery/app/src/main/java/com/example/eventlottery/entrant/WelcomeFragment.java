package com.example.eventlottery;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.model.Profile;
import com.example.eventlottery.organizer.OrganizerAccessCache;

/**
 * WelcomeFragment:
 * - Shows loading while checking device ID.
 * - If profile exists → auto-login.
 * - If not found → show registration form.
 */
public class WelcomeFragment extends Fragment {

    private EditText etName, etPhone, etEmail;
    private Button btnMainAction;
    private ProgressBar progressBar;
    private ProfileRepository profileRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);
        btnMainAction = view.findViewById(R.id.btnMainAction);
        progressBar = view.findViewById(R.id.progressBar);

        profileRepo = RepositoryProvider.getProfileRepository();

        // Initially hide form
        hideRegistrationForm();

        // Check if profile exists
        String deviceID = getDeviceId();
        progressBar.setVisibility(View.VISIBLE);

        profileRepo.findUserById(deviceID, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                OrganizerAccessCache.setAllowed(deviceID, profile != null && profile.isOrganizer());
                // Existing user → skip registration
                navigateToDashboard(view);
            }

            @Override
            public void onDeleted() {
                // No user found → show registration form
                progressBar.setVisibility(View.GONE);
                showRegistrationForm();
            }

            @Override
            public void onError(String message) {
                // Error or no record → show registration form
                progressBar.setVisibility(View.GONE);
                showRegistrationForm();
            }
        });

        // Registration button
        btnMainAction.setOnClickListener(v -> registerUser(view));
    }

    private void hideRegistrationForm() {
        etName.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        etPhone.setVisibility(View.GONE);
        btnMainAction.setVisibility(View.GONE);
    }

    private void showRegistrationForm() {
        etName.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.VISIBLE);
        etPhone.setVisibility(View.VISIBLE);
        btnMainAction.setVisibility(View.VISIBLE);
    }

    private void registerUser(@NonNull View view) {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String deviceID = getDeviceId();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        hideRegistrationForm();

        Profile newProfile = new Profile(deviceID, name, email, phone, true);
        profileRepo.saveUser(newProfile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                progressBar.setVisibility(View.GONE);
                OrganizerAccessCache.setAllowed(deviceID, profile != null && profile.isOrganizer());
                Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                navigateToDashboard(view);
            }

            @Override
            public void onDeleted() {}

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                showRegistrationForm();
                Toast.makeText(getContext(), "Registration failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDeviceId() {
        return Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void navigateToDashboard(@NonNull View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_welcomeFragment_to_entrantEventListFragment);
    }
}
