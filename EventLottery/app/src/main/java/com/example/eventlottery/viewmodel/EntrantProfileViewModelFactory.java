package com.example.eventlottery.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventlottery.data.ProfileRepository;

public class EntrantProfileViewModelFactory implements ViewModelProvider.Factory {
    private final ProfileRepository repository;

    public EntrantProfileViewModelFactory(ProfileRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EntrantProfileViewModel.class)) {
            return (T) new EntrantProfileViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
