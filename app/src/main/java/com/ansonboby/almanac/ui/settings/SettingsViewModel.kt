package com.ansonboby.almanac.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ansonboby.almanac.data.export.LedgerExport
import com.ansonboby.almanac.data.reminder.ReminderScheduler
import com.ansonboby.almanac.data.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Source of truth for theme + Phase 3 toggles (PRD 6 / Phase 3). */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val scheduler: ReminderScheduler,
    private val export: LedgerExport,
) : ViewModel() {

    val themeMode: Flow<Int> = prefs.themeMode
    val geotagEnabled: Flow<Boolean> = prefs.geotagEnabled
    val reminderEnabled: Flow<Boolean> = prefs.reminderEnabled
    val reminderTime: Flow<Pair<Int, Int>> = prefs.reminderTime

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setTheme(mode: Int) = viewModelScope.launch { prefs.setThemeMode(mode) }

    fun setGeotag(on: Boolean) = viewModelScope.launch { prefs.setGeotagEnabled(on) }

    fun setReminder(on: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setReminderEnabled(on)
            prefs.setReminderTime(hour, minute)
            if (on) scheduler.schedule(hour, minute) else scheduler.cancel()
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setReminderTime(hour, minute)
            if (prefs.reminderEnabled.first()) scheduler.schedule(hour, minute)
        }
    }

    fun backup(uri: Uri) = viewModelScope.launch {
        runCatching { export.backupToUri(uri) }
            .onSuccess { _message.value = "Ledger backed up." }
            .onFailure { _message.value = "Backup failed." }
    }

    /** On success the process restarts to reload the restored database. */
    fun restore(uri: Uri) {
        viewModelScope.launch {
            val ok = runCatching { export.restoreFromUri(uri) }.getOrDefault(false)
            if (!ok) _message.value = "Restore failed — not an Almanac backup."
        }
    }

    fun exportPdf(uri: Uri) = viewModelScope.launch {
        runCatching { export.exportPdfToUri(uri) }
            .onSuccess { _message.value = "Field report exported." }
            .onFailure { _message.value = "Export failed." }
    }

    fun purge() = viewModelScope.launch {
        runCatching { export.purge() }
            .onSuccess { _message.value = "Local ledger purged." }
            .onFailure { _message.value = "Purge failed." }
    }

    fun clearMessage() { _message.value = null }
}
