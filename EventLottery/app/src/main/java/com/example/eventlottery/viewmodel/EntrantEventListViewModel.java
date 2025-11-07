package com.example.eventlottery.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eventlottery.data.EventRepository;
import com.example.eventlottery.entrant.EventListUiState;
import com.example.eventlottery.model.Event;

import java.util.Collections;
import java.util.List;

/**
 * view model exposing entrant event list.
 */
public class EntrantEventListViewModel extends ViewModel {

    private final EventRepository repository;
    private final MediatorLiveData<EventListUiState> stateLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(true);

    public EntrantEventListViewModel(@NonNull EventRepository repository) {
        this.repository = repository;
        stateLiveData.setValue(EventListUiState.loading());
        LiveData<List<Event>> source = repository.observeEvents();
        stateLiveData.addSource(source, events -> {
            loadingLiveData.setValue(false);
            stateLiveData.setValue(new EventListUiState(false, events, null));
        });
        refresh();
    }

    @NonNull
    public LiveData<EventListUiState> getState() {
        return stateLiveData;
    }

    public void refresh() {
        loadingLiveData.setValue(true);
        stateLiveData.setValue(new EventListUiState(true, getCurrentEvents(), null));
        repository.refresh();
    }

    @NonNull
    public List<Event> getCurrentEvents() {
        EventListUiState value = stateLiveData.getValue();
        if (value == null) {
            return Collections.emptyList();
        }
        return value.events;
    }

}
