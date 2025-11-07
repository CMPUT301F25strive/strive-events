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
        etEmail = view.findViewById(R.id.etEmail);
        btnMainAction = view.findViewById(R.id.btnMainAction);
        tvSwitchMode = view.findViewById(R.id.tvSwitchMode);
        progressBar = view.findViewById(R.id.progressBar);

        profileRepo = RepositoryProvider.getProfileRepository();

        // Start in login mode by default
        setModeUI(isLoginMode);

        // ===== Auto-login by deviceID =====
        String deviceID = getDeviceId();
        progressBar.setVisibility(View.VISIBLE);
        profileRepo.findUserById(deviceID, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                progressBar.setVisibility(View.GONE);
                handleAdminEntry(view, profile);
            }

            @Override
            public void onDeleted() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                // stay in login/register screen
            }
        });

        // Main button action
        btnMainAction.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String deviceID1 = getDeviceId();
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (isLoginMode) {
                // Login mode
                profileRepo.findUserById(deviceID1, new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(Profile profile) {
                        progressBar.setVisibility(View.GONE);
                        handleAdminEntry(view, profile);
                    }

                    @Override
                    public void onDeleted() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Login failed: Device ID not found", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Register mode
                if (name.isEmpty() || email.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Profile newProfile = new Profile(deviceID1, name, email, phone);
                profileRepo.saveUser(newProfile, new ProfileRepository.ProfileCallback() {
                    @Override
                    public void onSuccess(Profile profile) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                        handleAdminEntry(view, profile);
                    }

                    @Override
                    public void onDeleted() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Registration failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Toggle mode on text click
        tvSwitchMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            setModeUI(isLoginMode);
        });
    }

    /**
     * Updates UI visibility/text based on the current mode.
     */
    private void setModeUI(boolean loginMode) {
        if (loginMode) {
            etName.setVisibility(View.GONE);
            etPhone.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE);
            btnMainAction.setText("Login");
            tvSwitchMode.setText("Not a user? Register");
        } else {
            etName.setVisibility(View.VISIBLE);
            etPhone.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            btnMainAction.setText("Register");
            tvSwitchMode.setText("Already a user? Login");
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