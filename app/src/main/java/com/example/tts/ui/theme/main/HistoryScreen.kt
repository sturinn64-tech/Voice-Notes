package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tts.data.model.AudioMessage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: MainUiState,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val pendingDelete = remember { mutableStateOf<AudioMessage?>(null) }

    pendingDelete.value?.let { msg ->
        AlertDialog(
            onDismissRequest = { pendingDelete.value = null},
            title = { Text("Удалить запись?") },
            text = { Text("Файл и запись были будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecording(msg, context)
                        pendingDelete.value = null
                    }
                ) { Text("Удалить")}
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete.value = null} ) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign out"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is MainUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MainUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    modifier = Modifier.padding(padding)
                )
            }

            is MainUiState.Success -> {
                if (uiState.messages.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No recordings yet")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { msg ->
                            val fileExists = File(context.cacheDir, msg.fileName).exists()
                            val isPlaying = viewModel.isPlaying(msg.fileName)

                            RecordingCard(
                                message = msg,
                                fileExists = fileExists,
                                isPlaying = isPlaying,
                                onPlay = { viewModel.playRecording(context, msg.fileName) },
                                onStop = { viewModel.stopCurrentPlayback() },
                                onDelete = { pendingDelete.value = msg}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordingCard(
    message: AudioMessage,
    fileExists: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(message.fileName, style = MaterialTheme.typography.titleMedium)

                Text(
                    text = message.timestamp.toDate().toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = message.transcript.ifBlank { "Транскрипт пока пустой" },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (fileExists) {
                IconButton(onClick = if (isPlaying) onStop else onPlay) {
                    Icon(
                        imageVector = if (isPlaying)
                            androidx.compose.material.icons.Icons.Filled.Close
                        else
                            androidx.compose.material.icons.Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play"
                    )
                }
            } else {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Warning,
                    contentDescription = "Missing"
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}