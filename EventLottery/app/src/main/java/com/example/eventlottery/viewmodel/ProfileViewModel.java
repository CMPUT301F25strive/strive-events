package com.example.eventlottery.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.entrant.ProfileUiState;
import com.example.eventlottery.model.Profile;

public class ProfileViewModel extends ViewModel {

    private final ProfileRepository repository;
    private final MutableLiveData<ProfileUiState> uiState = new MutableLiveData<>(ProfileUiState.loading());
    private String currentDeviceID;

    public ProfileViewModel(ProfileRepository repository) {
        this.repository = repository;
    }

    public LiveData<ProfileUiState> getUiState() {
        return uiState;
    }

    public void loadProfile(String deviceID) {
        uiState.setValue(ProfileUiState.loading());
        this.currentDeviceID = deviceID;
        Log.d("ProfileViewModel", "Loading profile for deviceId: " + deviceID);

        repository.findUserById(deviceID, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                uiState.postValue(ProfileUiState.success(profile));
            }

            @Override
            public void onDeleted() {
                // Not used in load
            }

            @Override
            public void onError(String error) {
                uiState.postValue(ProfileUiState.error(error));
            }
        });
    }

    public void updateProfile(String name, String email, String phone) {
        ProfileUiState cur = uiState.getValue();
        if (cur == null || cur.getProfile() == null) return;

        Profile profile = cur.getProfile();
        profile.updatePersonalInfo(name, email, phone);

        repository.saveUser(profile, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                uiState.postValue(ProfileUiState.success(profile));
            }

            @Override
            public void onDeleted() {
                // Not used in save
            }

            @Override
            public void onError(String message) {
                uiState.postValue(ProfileUiState.error(message));
            }
        });
    }

    public void deleteProfile() {
        ProfileUiState cur = uiState.getValue();
        if (cur == null || cur.getProfile() == null) return;

        String deviceId = cur.getProfile().getDeviceID();
        if (deviceId != null) {
            repository.deleteUser(deviceId, new ProfileRepository.ProfileCallback() {
                @Override
                public void onSuccess(Profile profile) {
                    // Not used in delete
                }

                @Override
                public void onDeleted() {
                    uiState.postValue(ProfileUiState.deleted());
                }

                @Override
                public void onError(String message) {
                    uiState.postValue(ProfileUiState.error(message));
                }
            });
        } else {
            uiState.setValue(ProfileUiState.error("Device ID is null"));
        }
    }
}