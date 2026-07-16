package com.ansonboby.almanac.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.repository.EntryFilter
import com.ansonboby.almanac.data.repository.EntryRepository
import com.ansonboby.almanac.data.util.LocalDateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class TodayUiState(
    val day: Int = LocalDateUtil.todayLocalDay(),
    val query: String = "",
    val filter: EntryFilter = EntryFilter.ALL,
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
        _uiState
            .map { Triple(it.day, it.query, it.filter) }
            .flatMapLatest { (day, query, filter) ->
                repository.dayEntries(day, query, filter)
                    .catch { emit(emptyList()) }
                    .map { entries -> day to entries }
            }
            .onEach { (day, entries) ->
                _uiState.value = _uiState.value.copy(day = day, entries = entries, isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun setDay(day: Int) {
        _uiState.value = _uiState.value.copy(day = day)
    }

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun setFilter(filter: EntryFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }
}
