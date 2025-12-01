package com.example.eventlottery.organizer;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.eventlottery.data.ProfileRepository;
import com.example.eventlottery.data.RepositoryProvider;
import com.example.eventlottery.model.Profile;

/**
 * Utility that determines whether the current device has organizer capabilities.
 */
public final class OrganizerGate {

    private OrganizerGate() {}

    /**
     * Determine whether the current device has organizer capabilities.
     * @param context
     * @return
     */
    public static boolean hasOrganizerAccess(Context context) {
        String deviceId = fetchDeviceId(context);
        if (TextUtils.isEmpty(deviceId)) {
            return false;
        }
        ProfileRepository repository = RepositoryProvider.getProfileRepository();
        Profile profile = repository.findUserById(deviceId);
        if (profile != null) {
            boolean allowed = profile.isOrganizer();
            OrganizerAccessCache.setAllowed(deviceId, allowed);
            return allowed;
        }
        return OrganizerAccessCache.isAllowed(deviceId);
    }

    /**
     * Fetch the device id.
     * @param context
     * @return
     */
    @Nullable
    private static String fetchDeviceId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }
}
