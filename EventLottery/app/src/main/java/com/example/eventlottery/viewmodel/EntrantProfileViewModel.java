package com.example.eventlottery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.entrant.ProfileUiState;
import com.example.eventlottery.model.EntrantProfile;

public class EntrantProfileViewModel extends ViewModel {
    private final String PRESET_PLACE_HOLDER = "NA";
    private final ProfileRepository repository;
    private final MutableLiveData<ProfileUiState> uiState = new MutableLiveData<>(ProfileUiState.loading());

    public EntrantProfileViewModel(ProfileRepository repository) {
        this.repository = repository;
        loadProfile();
    }

    public LiveData<ProfileUiState> getUiState() {
        return uiState;
    }

    public void loadProfile() {
        uiState.setValue(ProfileUiState.loading());
        try {
            EntrantProfile profile = repository.findEntrantById(PRESET_PLACE_HOLDER);   // preset in InMemProfileRepo
            uiState.setValue(ProfileUiState.success(profile));
        } catch (Exception e) {
            uiState.setValue(ProfileUiState.error("Failed to load profile"));
        }
    }

    public void updateProfile(String name, String email, String phone) {
        ProfileUiState cur = uiState.getValue();
        if (cur == null || cur.getProfile() == null) return;

        EntrantProfile profile = cur.getProfile();
        profile.updatePersonalInfo(name, email, phone);

        try {
            repository.saveEntrant(profile);
            uiState.setValue(ProfileUiState.success(profile));
        } catch (Exception e) {
            uiState.setValue(ProfileUiState.error("Failed to save profile"));
        }
    }
}
