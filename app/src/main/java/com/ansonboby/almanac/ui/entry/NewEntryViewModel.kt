package com.ansonboby.almanac.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.local.EntryType
import com.ansonboby.almanac.data.repository.EntryRepository
import com.ansonboby.almanac.data.util.LocalDateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewEntryUiState(
    val type: EntryType? = null,
    val text: String = "",
    val photoUri: String? = null,
    val caption: String = "",
    val moodScore: Int? = null,
    val tags: String = "",
    val stampingIn: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    private val repository: EntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState

    fun setType(type: EntryType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun setText(text: String) {
        _uiState.value = _uiState.value.copy(text = text, type = EntryType.TEXT)
    }

    fun setPhoto(uri: String, caption: String = "") {
        _uiState.value = _uiState.value.copy(photoUri = uri, caption = caption, type = EntryType.PHOTO)
    }

    fun setMood(score: Int) {
        _uiState.value = _uiState.value.copy(moodScore = score, type = EntryType.MOOD)
    }

    fun setTags(tags: String) {
        _uiState.value = _uiState.value.copy(tags = tags)
    }

    fun clearPhoto() {
        _uiState.value = _uiState.value.copy(photoUri = null, caption = "")
    }

    /** Persist immediately (PRD 7: no data loss) — "stamp into ledger". */
    fun stampIntoLedger(onDone: (Long) -> Unit) {
        val s = _uiState.value
        if (s.type == null && s.text.isBlank() && s.photoUri == null && s.moodScore == null) return
        _uiState.value = s.copy(stampingIn = true)
        viewModelScope.launch {
            val type = s.type ?: when {
                s.photoUri != null -> EntryType.PHOTO
                s.moodScore != null -> EntryType.MOOD
                else -> EntryType.TEXT
            }
            val entry = Entry(
                epochDayLocal = LocalDateUtil.todayLocalDay(),
                createdAt = LocalDateUtil.nowMillis(),
                type = type,
                textContent = s.text.takeIf { it.isNotBlank() },
                photoUri = s.photoUri,
                moodScore = s.moodScore,
                tags = s.tags.takeIf { it.isNotBlank() },
            )
            val id = repository.create(entry)
            _uiState.value = _uiState.value.copy(stampingIn = false, saved = true)
            onDone(id)
        }
    }
}
