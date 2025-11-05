package com.example.eventlottery;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventlottery.databinding.FragmentSecondBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreateProfilePage extends Fragment {

    private FragmentSecondBinding binding;
    private ProfileRepository profileDataList;
    private FirebaseFirestore db;
    private Button createProfileButton;
    private EditText nameEditText;
    private EditText phoneNumberEditText;
    private EditText emailEditText;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileDataList = new ProfileRepository();

        nameEditText = binding.nameEditText;
        emailEditText = binding.emailEditText;
        phoneNumberEditText = binding.phoneNumberEditText;


        createProfileButton.setOnClickListener(v -> {
            String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String userName = nameEditText.getText().toString();
            String userEmail = emailEditText.getText().toString();
            String userPhoneNumber = phoneNumberEditText.getText().toString();
            UserProfile user = new UserProfile(deviceID, userName, userEmail, userPhoneNumber);
            profileDataList.add(user);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}