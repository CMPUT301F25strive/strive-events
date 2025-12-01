package com.example.eventlottery.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventlottery.data.ProfileRepository;

/**
 * This is the view model factory for ProfileViewModel.
 */
public class ProfileViewModelFactory implements ViewModelProvider.Factory {
    private final ProfileRepository repository;

    /**
     * Constructor for ProfileViewModelFactory.
     * @param repository
     */
    public ProfileViewModelFactory(ProfileRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
