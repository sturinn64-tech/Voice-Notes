package com.example.tts.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(
    private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val DEFAULT_HISTORY_SORT = stringPreferencesKey("default_history_sort")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            themeMode = preferences[Keys.THEME_MODE]
                ?.let { safeEnumValueOf<AppThemeMode>(it) }
                ?: AppThemeMode.SYSTEM,
            confirmDelete = preferences[Keys.CONFIRM_DELETE] ?: true,
            defaultHistorySort = preferences[Keys.DEFAULT_HISTORY_SORT]
                ?.let { safeEnumValueOf<HistorySortOption>(it) }
                ?: HistorySortOption.NEWEST

        )
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateConfirmDelete(confirmDelete: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.CONFIRM_DELETE] = confirmDelete
        }
    }

    suspend fun updateDefaultHistorySort(sortOption: HistorySortOption) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.DEFAULT_HISTORY_SORT] = sortOption.name
        }
    }

    private inline fun <reified T : Enum<T>> safeEnumValueOf(value: String): T? {
        return runCatching { enumValueOf<T>(value) }.getOrNull()
    }
}
