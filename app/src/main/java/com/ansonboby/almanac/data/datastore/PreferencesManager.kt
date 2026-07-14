package com.ansonboby.almanac.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App preferences (DataStore). Holds the first-launch/onboarding gate and the
 * user's theme choice. No network, no accounts — everything local (PRD 7).
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val onboardingCompleteKey = booleanPreferencesKey("onboarding_complete")
    private val themeKey = intPreferencesKey("theme_mode") // 0 = system, 1 = ink(dark), 2 = parchment(light)

    val onboardingComplete: Flow<Boolean> =
        dataStore.data.map { it[onboardingCompleteKey] ?: false }

    val themeMode: Flow<Int> =
        dataStore.data.map { it[themeKey] ?: 0 }

    suspend fun setOnboardingComplete(value: Boolean) {
        dataStore.edit { it[onboardingCompleteKey] = value }
    }

    suspend fun setThemeMode(mode: Int) {
        dataStore.edit { it[themeKey] = mode }
    }
}
