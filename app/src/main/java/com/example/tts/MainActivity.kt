package com.example.tts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tts.ui.theme.TtsTheme
import com.example.tts.ui.theme.settings.SettingsViewModel
import androidx.compose.runtime.collectAsState


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(application)
            )
            val settings = settingsViewModel.uiState.collectAsState().value

            TtsTheme(themeMode = settings.themeMode) {
                AppNavigation(
                    appSettings = settings,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
