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

import com.example.eventlottery.admin.AdminGate;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.model.Profile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class WelcomeFragment extends Fragment {

    private EditText etName, etPhone, etEmail;
    private Button btnRegister;
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
        btnRegister = view.findViewById(R.id.btnMainAction);
        progressBar = view.findViewById(R.id.progressBar);

        profileRepo = RepositoryProvider.getProfileRepository();

        // Enable Firestore local cache to speed up queries
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        String deviceID = getDeviceId();
        progressBar.setVisibility(View.VISIBLE);

        // Use async admin-aware check first
        AdminGate.isAdmin(requireContext(), isAdmin -> {
            profileRepo.findUserById(deviceID, new ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(Profile profile) {
                    progressBar.setVisibility(View.GONE);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_welcomeFragment_to_entrantEventListFragment);
                }

                @Override
                public void onDeleted() {
                    progressBar.setVisibility(View.GONE);
                    showRegistrationForm();
                }

                @Override
                public void onError(String message) {
                    progressBar.setVisibility(View.GONE);
                    showRegistrationForm();
                }
            });
        });

        btnRegister.setOnClickListener(v -> registerNewUser(view, deviceID));
    }

    private void showRegistrationForm() {
        etName.setVisibility(View.VISIBLE);
        etPhone.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.VISIBLE);
    }

    private void registerNewUser(@NonNull View view, @NonNull String deviceID) {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Enter all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Profile newProfile = new Profile(deviceID, name, email, phone);
        profileRepo.saveUser(newProfile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view)
                        .navigate(R.id.action_welcomeFragment_to_entrantEventListFragment);
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

    private String getDeviceId() {
        return Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}