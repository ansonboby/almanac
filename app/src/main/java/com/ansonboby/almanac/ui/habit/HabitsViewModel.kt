package com.ansonboby.almanac.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.local.Habit
import com.ansonboby.almanac.data.local.HabitFrequency
import com.ansonboby.almanac.data.local.HabitWithStatus
import com.ansonboby.almanac.data.repository.HabitRepository
import com.ansonboby.almanac.data.util.LocalDateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitsUiState(
    val habits: List<HabitWithStatus> = emptyList(),
    val archived: List<Habit> = emptyList(),
    val showArchived: Boolean = false,
    val editorOpen: Boolean = false,
    val editingId: Long? = null,
    val title: String = "",
    val description: String = "",
    val frequencyKey: String = HabitFrequency.Daily.key,
    val customDays: String = "",
    val tint: String = "brass",
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            val day = LocalDateUtil.todayLocalDay()
            repository.habitsForDay(day)
                .catch { _uiState.value = _uiState.value.copy(habits = emptyList()) }
                .collect { habits ->
                    _uiState.value = _uiState.value.copy(habits = habits)
                }
        }
        viewModelScope.launch {
            repository.archivedHabits()
                .catch { }
                .collect { archived -> _uiState.value = _uiState.value.copy(archived = archived) }
        }
    }

    fun toggleToday(habitId: Long) {
        viewModelScope.launch { repository.toggleToday(habitId) }
    }

    fun setShowArchived(show: Boolean) {
        _uiState.value = _uiState.value.copy(showArchived = show)
    }

    // ---- editor sheet ------------------------------------------------------

    fun openNew() {
        _uiState.value = _uiState.value.copy(
            editorOpen = true,
            editingId = null,
            title = "",
            description = "",
            frequencyKey = HabitFrequency.Daily.key,
            customDays = "",
            tint = "brass",
        )
    }

    fun openEdit(habit: Habit) {
        _uiState.value = _uiState.value.copy(
            editorOpen = true,
            editingId = habit.id,
            title = habit.title,
            description = habit.description ?: "",
            frequencyKey = habit.frequency,
            customDays = habit.customDays ?: "",
            tint = habit.tint,
        )
    }

    fun closeEditor() {
        _uiState.value = _uiState.value.copy(editorOpen = false)
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun setFrequency(key: String) {
        _uiState.value = _uiState.value.copy(frequencyKey = key)
    }

    fun setCustomDays(days: String) {
        _uiState.value = _uiState.value.copy(customDays = days)
    }

    fun setTint(tint: String) {
        _uiState.value = _uiState.value.copy(tint = tint)
    }

    fun save() {
        val s = _uiState.value
        val title = s.title.trim()
        if (title.isBlank()) return
        viewModelScope.launch {
            val existing = s.editingId?.let { repository.loadHabit(it) }
            val habit = Habit(
                id = s.editingId ?: 0,
                title = title,
                description = s.description.trim().ifBlank { null },
                frequency = s.frequencyKey,
                customDays = if (s.frequencyKey == HabitFrequency.Custom.key) s.customDays else null,
                tint = s.tint,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                archived = existing?.archived ?: false,
            )
            if (s.editingId == null) repository.addHabit(habit) else repository.updateHabit(habit)
            _uiState.value = _uiState.value.copy(editorOpen = false)
        }
    }

    fun archive(habit: Habit) {
        viewModelScope.launch { repository.archiveHabit(habit, archived = true) }
    }
}
