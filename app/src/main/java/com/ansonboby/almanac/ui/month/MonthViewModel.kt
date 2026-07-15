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
import kotlinx.coroutines.launch
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

    init { observe() }

    fun goToMonth(centerDay: Int) {
        _uiState.value = _uiState.value.copy(centerDay = centerDay)
        observe()
    }

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        observe()
    }

    fun setFilter(filter: EntryFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        observe()
    }

    private fun observe() {
        val center = _uiState.value.centerDay
        val query = _uiState.value.query
        val filter = _uiState.value.filter
        viewModelScope.launch {
            repository.month(center, query, filter)
                .catch { _uiState.value = _uiState.value.copy(isLoading = false) }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        summaries = list.associateBy { it.day },
                        isLoading = false,
                    )
                }
        }
    }
}
