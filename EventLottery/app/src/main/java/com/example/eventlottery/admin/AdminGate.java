package com.example.eventlottery.admin;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.model.Profile;

/**
 * Utility class to determine if the current device user is an administrator.
 * This class provides a static method to check the user's role based on their device ID.
 */
public final class AdminGate {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AdminGate() {
    }

    /**
     * Checks if the current user has administrative privileges.
     * This method fetches the device's unique Android ID, retrieves the corresponding user profile
     * from the {@link ProfileRepository}, and checks if the user's role is {@code ADMIN}.
     *
     * @param context The application context, used to access the content resolver and repositories.
     * @return {@code true} if the user is an admin, {@code false} otherwise, or if the device ID cannot be fetched.
     */
    public static boolean isAdmin(Context context) {
        String deviceId = fetchDeviceId(context);
        if (TextUtils.isEmpty(deviceId)) {
            return false;
        }
        ProfileRepository repository = RepositoryProvider.getProfileRepository();
        // This is a synchronous call and will block the main thread.
        // It should only be used where the user profile is expected to be already cached or for quick, non-UI-blocking checks.
        Profile profile = repository.findUserById(deviceId);
        return profile != null && profile.isAdmin();
    }

    /**
     * Fetches the unique, stable identifier for the device.
     *
     * @param context The application context, needed to access the content resolver.
     * @return The {@link Settings.Secure#ANDROID_ID} as a string, or {@code null} if it cannot be retrieved.
     */
    @Nullable
    private static String fetchDeviceId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }
}