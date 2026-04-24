package com.example.tts.ui.theme.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tts.ui.components.AppScreenTopBar
import com.example.tts.ui.components.AppSectionCard
import com.example.tts.ui.components.AppSectionTitle
import com.example.tts.ui.components.AppStatusBadge

@Composable
fun MainScreen(
    uiState: MainUiState,
    hasRecordPermission: Boolean,
    onSaveRecording: (filePath: String) -> Unit,
    onClearError: () -> Unit,
    onClearInfo: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var permissionGranted by remember { mutableStateOf(hasRecordPermission) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(hasRecordPermission) {
        permissionGranted = hasRecordPermission
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
        onClearError()
    }

    LaunchedEffect(uiState.infoMessage) {
        val message = uiState.infoMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
        onClearInfo()
    }

    Scaffold(
        topBar = {
            AppScreenTopBar(title = "Запись")
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!permissionGranted) {
                    AppSectionCard {
                        AppStatusBadge(text = "Требуется разрешение")

                        AppSectionTitle(
                            title = "Доступ к микрофону",
                            subtitle = "Без него запись голосовых заметок не запустится"
                        )

                        Text(
                            text = "Разреши доступ к микрофону, чтобы приложение могло записывать и сохранять голосовые заметки на устройстве.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (granted) {
                                    permissionGranted = true
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Разрешить доступ")
                        }
                    }
                } else {
                    AudioRecorder(
                        onSaveRecording = { filePath ->
                            if (!uiState.isSaving) {
                                onSaveRecording(filePath)
                            }
                        }
                    )
                }
            }

            if (uiState.isSaving) {
                SavingOverlay()
            }
        }
    }
}

@Composable
private fun SavingOverlay() {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background.copy(alpha = 0.72f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AppSectionCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            AppStatusBadge(text = "Сохранение")

            AppSectionTitle(
                title = "Подожди немного",
                subtitle = "Запись сохраняется и распознаётся"
            )
        }
    }
}