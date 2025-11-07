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

public class WelcomeFragment extends Fragment {

    private EditText etEmail, etPassword, etPhone, etName;
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

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etPhone = view.findViewById(R.id.etPhone);
        etName = view.findViewById(R.id.etName);
        btnMainAction = view.findViewById(R.id.btnMainAction);
        tvSwitchMode = view.findViewById(R.id.tvSwitchMode);
        progressBar = view.findViewById(R.id.progressBar);

        profileRepo = com.example.eventlottery.data.RepositoryProvider.getProfileRepository();

        btnMainAction.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String deviceID = getDeviceId();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Email and password required", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            if (isLoginMode) {
                profileRepo.login(email, password, (success, message) -> {
                    progressBar.setVisibility(View.GONE);
                    if (success) {
                        Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view)
                                .navigate(R.id.action_welcomeFragment_to_entrantEventListFragment);
                    } else {
                        Toast.makeText(getContext(), "Login failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (name.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Please enter your full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                profileRepo.register(email, password, phone, name, deviceID, (success, message) -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    if (success) {
                        Navigation.findNavController(view)
                                .navigate(R.id.action_welcomeFragment_to_entrantEventListFragment);
                    }
                });
            }
        });

        tvSwitchMode.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        if (isLoginMode) {
            isLoginMode = false;
            etPhone.setVisibility(View.VISIBLE);
            etName.setVisibility(View.VISIBLE);
            btnMainAction.setText("Register");
            tvSwitchMode.setText("Already a user? Login");
        } else {
            isLoginMode = true;
            etPhone.setVisibility(View.GONE);
            etName.setVisibility(View.GONE);
            btnMainAction.setText("Login");
            tvSwitchMode.setText("Not a user? Register");
        }
    }

    private String getDeviceId() {
        return Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}