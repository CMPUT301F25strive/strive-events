package com.example.eventlottery.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.entrant.ProfileUiState;
import com.example.eventlottery.model.Profile;

/**
 * ViewModel for managing profile operations: load, update, delete.
 * Observed by ProfileFragment via uiState LiveData.
 */
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

    /**
     * Loads the profile for the given deviceID.
     */
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

    /**
     * Updates the current profile's personal information.
     */
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

    /**
     * Updates the notification setting of the user
     * On success, sets the notification setting of the user to the switch's current state
     */
    public void updateNotifications(boolean isChecked) {
        ProfileUiState cur = uiState.getValue();
        if (cur == null || cur.getProfile() == null) return;

        Profile profile = cur.getProfile();
        profile.setNotificationSettings(isChecked);

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
    /**
     * Deletes the current profile.
     * On success, sets uiState to deleted, triggering fragment navigation.
     */
    public void deleteProfile() {
        ProfileUiState cur = uiState.getValue();
        if (cur == null || cur.getProfile() == null) {
            uiState.setValue(ProfileUiState.error("No profile loaded"));
            return;
        }

        String deviceId = cur.getProfile().getDeviceID();
        if (deviceId == null || deviceId.isEmpty()) {
            uiState.setValue(ProfileUiState.error("Device ID is null or empty"));
            return;
        }

        repository.deleteUser(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                // Not used for delete
            }

            @Override
            public void onDeleted() {
                Log.d("ProfileViewModel", "Profile deleted successfully: " + deviceId);
                uiState.postValue(ProfileUiState.deleted());
            }

            @Override
            public void onError(String message) {
                Log.e("ProfileViewModel", "Error deleting profile: " + message);
                uiState.postValue(ProfileUiState.error(message));
            }
        });
    }
}