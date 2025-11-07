//package com.example.eventlottery.admin;
//
//import android.content.Context;
//import android.provider.Settings;
//import android.text.TextUtils;
//
//import androidx.annotation.Nullable;
//
//import com.example.eventlottery.data.ProfileRepository;
//import com.example.eventlottery.data.RepositoryProvider;
//import com.example.eventlottery.model.Profile;
//
///**
// * Utility to determine if the current device belongs to an admin.
// */
//public final class AdminGate {
//
//    private AdminGate() {
//    }
//
//    public static boolean isAdmin(Context context) {
//        String deviceId = fetchDeviceId(context);
//        if (TextUtils.isEmpty(deviceId)) {
//            return false;
//        }
//        ProfileRepository repository = RepositoryProvider.getProfileRepository();
//        Profile profile = repository.findUserById(deviceId);
//        return profile != null && profile.isAdmin();
//    }
//
//    @Nullable
//    private static String fetchDeviceId(Context context) {
//        return Settings.Secure.getString(
//                context.getContentResolver(),
//                Settings.Secure.ANDROID_ID
//        );
//    }
//}
