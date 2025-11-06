package com.example.eventlottery.entrant;

import androidx.annotation.NonNull;

import com.example.eventlottery.model.Profile;

public class ProfileUiState {
    private final boolean loading;
    private final Profile profile;
    private final String errorMessage;

    public ProfileUiState(boolean loading, Profile profile, String errorMessage) {
        this.loading = loading;
        this.profile = profile;
        this.errorMessage = errorMessage;
    }

    public boolean isLoading() { return loading; }
    public Profile getProfile() { return profile; }
    public String getErrorMessage() { return errorMessage; }

    @NonNull
    public static ProfileUiState loading() {
        return new ProfileUiState(true, null, null);
    }

    public static ProfileUiState success(Profile profile) {
        return new ProfileUiState(false, profile, null);
    }

    public static ProfileUiState error(String msg) {
        return new ProfileUiState(false, null, msg);
    }
}
