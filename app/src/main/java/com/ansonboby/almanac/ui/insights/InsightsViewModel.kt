package com.ansonboby.almanac.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.repository.DayCount
import com.ansonboby.almanac.data.repository.DayMood
import com.ansonboby.almanac.data.repository.EntryRepository
import com.ansonboby.almanac.data.repository.HabitConsistency
import com.ansonboby.almanac.data.repository.InsightsRepository
import com.ansonboby.almanac.data.repository.computeFrequency
import com.ansonboby.almanac.data.repository.computeMoodTrend
import com.ansonboby.almanac.data.util.LocalDateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val periodDays: Int = 30,
    val mood: List<DayMood> = emptyList(),
    val frequency: List<DayCount> = emptyList(),
    val habits: List<HabitConsistency> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val insightsRepository: InsightsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState

    init {
        observe()
    }

    private fun observe() {
        val period = 30
        val end = LocalDateUtil.todayLocalDay()
        val start = end - (period - 1)
        viewModelScope.launch {
            entryRepository.allEntries()
                .catch { }
                .collect { entries: List<Entry> ->
                    val mood = computeMoodTrend(entries, start, end)
                    val frequency = computeFrequency(entries, start, end)
                    val habits = runCatching { insightsRepository.habitConsistency(period) }.getOrDefault(emptyList())
                    _uiState.value = InsightsUiState(
                        periodDays = period,
                        mood = mood,
                        frequency = frequency,
                        habits = habits,
                        isLoading = false,
                    )
                }
        }
    }
}
