package com.ansonboby.almanac.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.repository.EntryRepository
import com.ansonboby.almanac.data.util.LocalDateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val day: Int = LocalDateUtil.todayLocalDay(),
    val entries: List<Entry> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: EntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState

    init {
        observeDay(LocalDateUtil.todayLocalDay())
    }

    private fun observeDay(day: Int) {
        viewModelScope.launch {
            repository.dayEntries(day)
                .catch { _uiState.value = TodayUiState(day = day, isLoading = false) }
                .collect { entries ->
                    _uiState.value = TodayUiState(day = day, entries = entries, isLoading = false)
                }
        }
    }
}
