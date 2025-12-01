package com.example.eventlottery.organizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.eventlottery.model.AppContextProvider;

/**
 * Simple SharedPreferences-backed cache so organizer gates can keep working
 * even when Firestore data is still loading.
 */
public final class OrganizerAccessCache {

    private static final String PREF_NAME = "organizer_access_prefs";
    private static final String KEY_PREFIX = "can_host_";

    private OrganizerAccessCache() { }

    public static void setAllowed(@Nullable String deviceId, boolean allowed) {
        SharedPreferences prefs = getPrefs();
        if (prefs == null || TextUtils.isEmpty(deviceId)) return;
        prefs.edit().putBoolean(KEY_PREFIX + deviceId, allowed).apply();
    }

    public static boolean isAllowed(@Nullable String deviceId) {
        SharedPreferences prefs = getPrefs();
        if (prefs == null || TextUtils.isEmpty(deviceId)) return false;
        return prefs.getBoolean(KEY_PREFIX + deviceId, false);
    }

    public static void clear(@Nullable String deviceId) {
        SharedPreferences prefs = getPrefs();
        if (prefs == null || TextUtils.isEmpty(deviceId)) return;
        prefs.edit().remove(KEY_PREFIX + deviceId).apply();
    }

    @Nullable
    private static SharedPreferences getPrefs() {
        Context context = AppContextProvider.getContext();
        if (context == null) {
            return null;
        }
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
