package com.example.eventlottery.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MyEventsViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public MyEventsViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MyEventsViewModel.class)) {
            return (T) new MyEventsViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}