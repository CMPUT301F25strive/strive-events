package com.example.eventlottery.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.eventlottery.TestLiveDataUtil;
import com.example.eventlottery.data.FakeEventRepository;
import com.example.eventlottery.entrant.EventListUiState;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EntrantEventListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void initialState_containsSeedEvents() throws InterruptedException {
        EntrantEventListViewModel viewModel = new EntrantEventListViewModel(new FakeEventRepository());
        EventListUiState state = TestLiveDataUtil.getOrAwaitValue(viewModel.getState());
        assertFalse(state.loading);
        assertEquals(4, state.events.size());
    }

    @Test
    public void refresh_emitsLoadingFollowedByData() throws InterruptedException {
        EntrantEventListViewModel viewModel = new EntrantEventListViewModel(new FakeEventRepository());
        List<EventListUiState> captured = new ArrayList<>();
        androidx.lifecycle.Observer<EventListUiState> observer = captured::add;
        viewModel.getState().observeForever(observer);
        try {
            viewModel.refresh();
            assertFalse(captured.isEmpty());
            assertTrue(captured.stream().anyMatch(state -> state.loading));
            EventListUiState latest = captured.get(captured.size() - 1);
            assertFalse(latest.loading);
            assertEquals(4, latest.events.size());
        } finally {
            viewModel.getState().removeObserver(observer);
        }
    }
}
