package com.example.eventlottery.admin;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.model.Profile;

/**
 * Utility to determine if the current device belongs to an admin.
 */
public final class AdminGate {

    private AdminGate() { }

    // Callback interface
    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    // Asynchronous admin check
    public static void isAdmin(Context context, AdminCheckCallback callback) {
        String deviceId = fetchDeviceId(context);
        if (TextUtils.isEmpty(deviceId)) {
            callback.onResult(false);
            return;
        }

        ProfileRepository repository = RepositoryProvider.getProfileRepository();
        repository.findUserById(deviceId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                callback.onResult(profile != null && profile.isAdmin());
            }

            @Override
            public void onDeleted() {
                callback.onResult(false);
            }

            @Override
            public void onError(String message) {
                callback.onResult(false);
            }
        });
    }

    @Nullable
    private static String fetchDeviceId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }
}