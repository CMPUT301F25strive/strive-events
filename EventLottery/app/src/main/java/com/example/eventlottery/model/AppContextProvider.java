package com.example.eventlottery.model;

import android.content.Context;

/**
 * This class provides a static method to get the application context.
 */

public class AppContextProvider extends android.app.Application {
    private static AppContextProvider instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }
}