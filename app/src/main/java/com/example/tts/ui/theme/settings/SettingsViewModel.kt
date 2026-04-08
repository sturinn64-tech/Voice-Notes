package com.example.tts.ui.theme.settings

import android.app.Application
import androidx.annotation.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tts.data.settings.AppSettings
import com.example.tts.data.settings.AppThemeMode
import com.example.tts.data.settings.HistorySortOption
import com.example.tts.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val uiState = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun updateConfirmDelete(confirmDelete: Boolean) {
        viewModelScope.launch {
            repository.updateConfirmDelete(confirmDelete)
        }
    }

    fun updateDefaultHistorySort(sortOption: HistorySortOption) {
        viewModelScope.launch {
            repository.updateDefaultHistorySort(sortOption)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
           return object : ViewModelProvider.Factory {
               @Suppress("UNCHECKED_CAST")
               override fun <T : ViewModel> create(modelClass: Class<T>): T {
                   if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                       return SettingsViewModel(application) as T
                   }
                   throw IllegalArgumentException("Unknown ViewModel class")
               }
           }
        }
    }
}
