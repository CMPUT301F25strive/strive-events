package com.example.eventlottery.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventlottery.data.RepositoryProvider;

/**
 * provides entrant event list view models with shared repositories.
 */
public class EntrantEventListViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EntrantEventListViewModel.class)) {
            return (T) new EntrantEventListViewModel(RepositoryProvider.getEventRepository());
        }
        throw new IllegalArgumentException("unknown viewmodel type " + modelClass.getName());
    }
}
