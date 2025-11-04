package com.example.eventlottery.entrant;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.R;
import com.example.eventlottery.data.InMemProfileRepo;
import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.model.EntrantProfile;
import com.example.eventlottery.viewmodel.EntrantProfileViewModel;
import com.example.eventlottery.viewmodel.EntrantProfileViewModelFactory;
import com.google.android.material.appbar.MaterialToolbar;

public class EditProfileFragment extends Fragment {

    private EntrantProfileViewModel viewModel;
    private EditText editTextName, editTextEmail, editTextPhone;
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

        // Shared ViewModel for profile + edit profile (activity scope)
        ProfileRepository repo = new InMemProfileRepo(); // replace with real repo later
        EntrantProfileViewModelFactory factory = new EntrantProfileViewModelFactory(repo);
        viewModel = new ViewModelProvider(requireActivity(), factory)
                .get(EntrantProfileViewModel.class);

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
        });

        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.entrantEventListFragment, false)
        );

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            // Validation step
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.updateProfile(name, email, phone);
            NavHostFragment.findNavController(this).popBackStack();
        });

    }
}
