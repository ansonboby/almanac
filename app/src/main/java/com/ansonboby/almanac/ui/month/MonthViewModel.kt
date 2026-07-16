package com.ansonboby.almanac.ui.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.local.DaySummary
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

data class MonthUiState(
    val centerDay: Int = LocalDateUtil.todayLocalDay(),
    val query: String = "",
    val filter: EntryFilter = EntryFilter.ALL,
    val summaries: Map<Int, DaySummary> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class MonthViewModel @Inject constructor(
    private val repository: EntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthUiState())
    val uiState: StateFlow<MonthUiState> = _uiState

    init {
        _uiState
            .map { Triple(it.centerDay, it.query, it.filter) }
            .flatMapLatest { (center, query, filter) ->
                repository.month(center, query, filter)
                    .catch { emit(emptyList()) }
                    .map { summaries -> center to summaries }
            }
            .onEach { (center, list) ->
                _uiState.value = _uiState.value.copy(
                    centerDay = center,
                    summaries = list.associateBy { it.day },
                    isLoading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    fun goToMonth(centerDay: Int) {
        _uiState.value = _uiState.value.copy(centerDay = centerDay)
    }

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun setFilter(filter: EntryFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }
}
