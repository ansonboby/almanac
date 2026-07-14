package com.ansonboby.almanac.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EntryDetailUiState(
    val entry: Entry? = null,
    val deleted: Boolean = false,
)

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val repository: EntryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val entryId: Long = checkNotNull(savedStateHandle.get<Long>("entryId"))

    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.entry(entryId).collect { entry ->
                _uiState.value = _uiState.value.copy(entry = entry)
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            val e = _uiState.value.entry ?: return@launch
            repository.delete(e)
            _uiState.value = _uiState.value.copy(deleted = true)
            onDone()
        }
    }

    /** Re-stamp an edited entry (text/caption/tags/mood) back into the ledger. */
    fun saveEdit(
        text: String,
        caption: String,
        tags: String,
        moodScore: Int?,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val e = _uiState.value.entry ?: return@launch
            val updated = e.copy(
                textContent = text.takeIf { it.isNotBlank() },
                photoUri = e.photoUri,
                moodScore = moodScore,
                tags = tags.takeIf { it.isNotBlank() },
            )
            repository.update(updated)
            onDone()
        }
    }
}
