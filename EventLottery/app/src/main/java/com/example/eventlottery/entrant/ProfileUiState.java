package com.example.eventlottery.entrant;

import androidx.annotation.NonNull;
import com.example.eventlottery.model.Profile;

public class ProfileUiState {
    private final boolean loading;
    private final Profile profile;
    private final String errorMessage;
    private final boolean deleted;

    public ProfileUiState(boolean loading, Profile profile, String errorMessage, boolean deleted) {
        this.loading = loading;
        this.profile = profile;
        this.errorMessage = errorMessage;
        this.deleted = deleted;
    }

    public boolean isLoading() {
        return loading;
    }

    public Profile getProfile() {
        return profile;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @NonNull
    public static ProfileUiState loading() {
        return new ProfileUiState(true, null, null, false);
    }

    public static ProfileUiState success(Profile profile) {
        return new ProfileUiState(false, profile, null, false);
    }

    public static ProfileUiState error(String msg) {
        return new ProfileUiState(false, null, msg, false);
    }

    public static ProfileUiState deleted() {
        return new ProfileUiState(false, null, "Profile deleted successfully", true);
    }
}