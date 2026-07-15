package com.ansonboby.almanac.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.datastore.PreferencesManager
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.local.EntryType
import com.ansonboby.almanac.data.location.GeoTag
import com.ansonboby.almanac.data.location.LocationRepository
import com.ansonboby.almanac.data.repository.EntryRepository
import com.ansonboby.almanac.data.util.LocalDateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class NewEntryUiState(
    val type: EntryType? = EntryType.TEXT,
    val text: String = "",
    val photoUri: String? = null,
    val caption: String = "",
    val moodScore: Int? = null,
    val tags: String = "",
    val geoTag: GeoTag? = null,
    val stampingIn: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val locationRepository: LocationRepository,
    private val prefs: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewEntryUiState())
    val uiState: StateFlow<NewEntryUiState> = _uiState

    val geotagEnabled: Flow<Boolean> = prefs.geotagEnabled

    fun setType(type: EntryType) {
        _uiState.value = _uiState.value.copy(type = type, errorMessage = null)
    }

    fun setText(text: String) {
        _uiState.value = _uiState.value.copy(text = text, type = EntryType.TEXT, errorMessage = null)
    }

    fun setPhoto(uri: String, caption: String = "") {
        _uiState.value = _uiState.value.copy(photoUri = uri, caption = caption, type = EntryType.PHOTO, errorMessage = null)
    }

    fun setMood(score: Int) {
        _uiState.value = _uiState.value.copy(moodScore = score, type = EntryType.MOOD, errorMessage = null)
    }

    fun setTags(tags: String) {
        _uiState.value = _uiState.value.copy(tags = tags)
    }

    fun setGeoTag(tag: GeoTag?) {
        _uiState.value = _uiState.value.copy(geoTag = tag)
    }

    fun clearPhoto() {
        _uiState.value = _uiState.value.copy(photoUri = null, caption = "", errorMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /** Capture the current place (opt-in geotag). Returns true if a tag was set. */
    suspend fun captureLocation(): Boolean {
        val tag = try {
            locationRepository.currentGeoTag()
        } catch (_: Exception) {
            null
        }
        _uiState.value = _uiState.value.copy(geoTag = tag)
        return tag != null
    }

    /** Fire-and-forget geotag capture using the ViewModel's own scope. */
    fun requestGeoTag() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        viewModelScope.launch { captureLocation() }
    }

    /** Persist immediately (PRD 7: no data loss) — "stamp into ledger". */
    fun stampIntoLedger(onDone: (Long) -> Unit) {
        val s = _uiState.value
        if (s.type == null && s.text.isBlank() && s.photoUri == null && s.moodScore == null) return
        _uiState.value = s.copy(stampingIn = true, errorMessage = null)
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
                locationName = s.geoTag?.name,
                lat = s.geoTag?.lat,
                lng = s.geoTag?.lng,
                tags = s.tags.takeIf { it.isNotBlank() },
            )
            try {
                val id = withContext(Dispatchers.IO) { repository.create(entry) }
                _uiState.value = _uiState.value.copy(stampingIn = false, saved = true)
                onDone(id)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(stampingIn = false, errorMessage = "SAVE FAILED — TRY AGAIN")
            }
        }
    }
}
