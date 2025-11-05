package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.model.EntrantProfile;
import com.example.eventlottery.viewmodel.ProfileViewModel;
import com.example.eventlottery.viewmodel.ProfileViewModelFactory;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileEditFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextInputEditText editTextName, editTextEmail, editTextPhone;
    private Button buttonSave;
    private MaterialToolbar toolbar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        toolbar = view.findViewById(R.id.editProfileToolbar);
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        buttonSave = view.findViewById(R.id.buttonSave);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProfileRepository repo = RepositoryProvider.getProfileRepository();
        ProfileViewModelFactory factory = new ProfileViewModelFactory(repo);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(ProfileViewModel.class);

        // Fill fields with current data once
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state.getProfile() != null) {
                EntrantProfile p = state.getProfile();
                if (editTextName.getText().length() == 0
                        && editTextEmail.getText().length() == 0
                        && editTextPhone.getText().length() == 0) {
                    editTextName.setText(p.getName());
                    editTextEmail.setText(p.getEmail());
                    editTextPhone.setText(p.getPhone());
                }
            }
            if (state != null && state.getErrorMessage() != null) {
                Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack()
        );

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            // Validation
            if (name.isEmpty()) {
                editTextName.setError("Name cannot be empty");
                return;
            }
            if (email.isEmpty()) {
                editTextEmail.setError("Email cannot be empty");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextEmail.setError("Please enter a valid email format");
                return;
            }

            // Clear errors
            editTextName.setError(null);
            editTextEmail.setError(null);

            viewModel.updateProfile(name, email, phone);
            
            // Show success message and navigate back
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        });

    }
}