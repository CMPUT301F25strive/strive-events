package com.example.eventlottery.model;

import android.content.Context;

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