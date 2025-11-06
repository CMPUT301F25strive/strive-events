package com.example.eventlottery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.entrant.ProfileUiState;
import com.example.eventlottery.model.Profile;

public class ProfileViewModel extends ViewModel {
    private final ProfileRepository repository;
    private final MutableLiveData<ProfileUiState> uiState = new MutableLiveData<>(ProfileUiState.loading());
    private String currentDeviceId;

    public ProfileViewModel(ProfileRepository repository) {
        this.repository = repository;
    }

    public LiveData<ProfileUiState> getUiState() {
        return uiState;
    }

    public void loadProfile(String deviceId) {
        uiState.setValue(ProfileUiState.loading());
        this.currentDeviceId = deviceId;
            
        try {
            Profile profile = repository.findUserById(deviceId);
            if (profile != null) {
            uiState.setValue(ProfileUiState.success(profile));
            }
        } catch (Exception e) {
            uiState.setValue(ProfileUiState.error("Failed to load profile"));
        }
    }

    public void updateProfile(String name, String email, String phone) {
        ProfileUiState cur = uiState.getValue();
        if (cur == null || cur.getProfile() == null) return;

        Profile profile = cur.getProfile();
        profile.updatePersonalInfo(name, email, phone);

        try {
            repository.saveUser(profile);
            uiState.setValue(ProfileUiState.success(profile));
        } catch (Exception e) {
            uiState.setValue(ProfileUiState.error("Failed to save profile"));
        }
    }
    public void deleteProfile() {
        try {
            ProfileUiState cur = uiState.getValue();
            if (cur != null && cur.getProfile() != null) {
                String deviceId = cur.getProfile().getDeviceID();

                if (deviceId != null) {
                    repository.deleteUser(deviceId);
                    uiState.setValue(ProfileUiState.deleted()); // Use the new deleted state
                }
            }
        } catch (Exception e) {
            uiState.setValue(ProfileUiState.error("Failed to delete profile"));
        }
    }
}