package com.example.eventlottery;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.model.Profile;
import com.google.android.material.snackbar.Snackbar;

/**
 * This is the Javacode for the welcome page. If user's deviced ID is in database, it will automatically navagiate to main page.
 */

public class WelcomeFragment extends Fragment {

    private EditText etName, etPhone, etEmail;
    private Button btnMainAction;
    private TextView tvSwitchMode;
    private ProgressBar progressBar;
    private ProfileRepository profileRepo;

    private boolean isLoginMode = true;

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
        etEmail = view.findViewById(R.id.etEmail); // Add email EditText in XML
        btnMainAction = view.findViewById(R.id.btnMainAction);
        tvSwitchMode = view.findViewById(R.id.tvSwitchMode);
        progressBar = view.findViewById(R.id.progressBar);

        profileRepo = RepositoryProvider.getProfileRepository();

        // ===== Auto-login by deviceID =====
        String deviceID = getDeviceId();
        progressBar.setVisibility(View.VISIBLE);
        profileRepo.findUserById(deviceID, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                // DeviceID exists → auto-login
                progressBar.setVisibility(View.GONE);
                handleAdminEntry(view, profile);
            }

            @Override
            public void onDeleted() { }

            @Override
            public void onError(String message) {
                // User not found → stay in login/register screen
                progressBar.setVisibility(View.GONE);
            }
        });

        // Main action button
        btnMainAction.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String phone = etPhone.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim(); // Get email input

            if (!isLoginMode && email.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                profileRepo.findUserById(deviceID, new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(Profile profile) {
                        progressBar.setVisibility(View.GONE);
                        // DeviceID exists → login successful
                        handleAdminEntry(view, profile);
                    }

                    @Override
                    public void onDeleted() { }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Login failed: Device ID not found", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (name.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Enter full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                Profile newProfile = new Profile(deviceID, name, email, phone); // Pass email here
                profileRepo.saveUser(newProfile, new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(Profile profile) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                        handleAdminEntry(view, profile);
                    }

                    @Override
                    public void onDeleted() { }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Registration failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Toggle login/register mode
        tvSwitchMode.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        if (isLoginMode) {
            isLoginMode = false;
            etPhone.setVisibility(View.VISIBLE);
            etName.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE); // show email in register mode
            btnMainAction.setText("Register");
            tvSwitchMode.setText("Already a user? Login");
        } else {
            isLoginMode = true;
            etPhone.setVisibility(View.GONE);
            etName.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE); // hide email in login mode
            btnMainAction.setText("Login");
            tvSwitchMode.setText("Not a user? Register");
        }
    }

    private String getDeviceId() {
        return Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void handleAdminEntry(@NonNull View view, @Nullable Profile profile) {
        boolean isAdmin = profile != null && profile.isAdmin();
        if (isAdmin) {
            Snackbar.make(view, R.string.admin_welcome_snackbar, Snackbar.LENGTH_LONG).show();
        }
        navigateToDashboard(view);
    }

    private void navigateToDashboard(@NonNull View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_welcomeFragment_to_entrantEventListFragment);
    }
}
