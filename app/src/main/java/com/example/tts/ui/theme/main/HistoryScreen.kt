package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tts.data.model.AudioMessage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class HistorySort(val title: String) {
    NEWEST("Сначала новые"),
    OLDEST("Сначала старые"),
    TITLE("По названию")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: MainUiState,
    onSignOut: () -> Unit,
    viewModel: MainViewModel
) {
    val pendingDelete = remember { mutableStateOf<AudioMessage?>(null) }
    val pendingEdit = remember { mutableStateOf<AudioMessage?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(HistorySort.NEWEST) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    pendingDelete.value?.let { msg ->
        AlertDialog(
            onDismissRequest = { pendingDelete.value = null },
            title = { Text("Удалить запись?") },
            text = { Text("Файл и запись будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecording(msg)
                        pendingDelete.value = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDelete.value = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    pendingEdit.value?.let { msg ->
        EditRecordingDialog(
            message = msg,
            onDismiss = { pendingEdit.value = null },
            onSave = { newTitle, newTranscript ->
                viewModel.updateMessage(
                    message = msg,
                    newTitle = newTitle,
                    newTranscript = newTranscript
                )
                pendingEdit.value = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Выйти"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is MainUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MainUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ошибка: ${uiState.message}")
                }
            }

            is MainUiState.Success -> {
                val filteredMessages by remember(
                    uiState.messages,
                    searchQuery,
                    favoritesOnly,
                    selectedSort
                ) {
                    derivedStateOf {
                        val normalizedQuery = searchQuery.trim()

                        val filtered = uiState.messages.filter { message ->
                            val matchesFavorite = !favoritesOnly || message.isFavorite

                            val searchableText = buildString {
                                append(message.title)
                                append(" ")
                                append(message.fileName)
                                append(" ")
                                append(message.transcript)
                            }

                            val matchesQuery = normalizedQuery.isBlank() ||
                                    searchableText.contains(normalizedQuery, ignoreCase = true)

                            matchesFavorite && matchesQuery
                        }

                        when (selectedSort) {
                            HistorySort.NEWEST -> filtered.sortedWith(
                                compareByDescending<AudioMessage> { it.isFavorite }
                                    .thenByDescending { it.createdAt }
                            )

                            HistorySort.OLDEST -> filtered.sortedWith(
                                compareByDescending<AudioMessage> { it.isFavorite }
                                    .thenBy { it.createdAt }
                            )

                            HistorySort.TITLE -> filtered.sortedWith(
                                compareByDescending<AudioMessage> { it.isFavorite }
                                    .thenBy {
                                        it.title
                                            .ifBlank { it.fileName }
                                            .lowercase(Locale.getDefault())
                                    }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Поиск") },
                                placeholder = { Text("По названию и транскрипту") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Только избранное",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = favoritesOnly,
                                        onCheckedChange = { favoritesOnly = it }
                                    )
                                }

                                Box {
                                    OutlinedButton(
                                        onClick = { sortMenuExpanded = true }
                                    ) {
                                        Text(selectedSort.title)
                                    }

                                    DropdownMenu(
                                        expanded = sortMenuExpanded,
                                        onDismissRequest = { sortMenuExpanded = false }
                                    ) {
                                        HistorySort.entries.forEach { sort ->
                                            DropdownMenuItem(
                                                text = { Text(sort.title) },
                                                onClick = {
                                                    selectedSort = sort
                                                    sortMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Найдено записей: ${filteredMessages.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (uiState.messages.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Записей пока нет")
                        }
                    } else if (filteredMessages.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ничего не найдено по текущим фильтрам")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filteredMessages,
                                key = { it.id }
                            ) { msg ->
                                val fileExists = File(msg.filePath).exists()
                                val isPlaying = viewModel.isPlaying(msg.filePath)

                                RecordingCard(
                                    message = msg,
                                    fileExists = fileExists,
                                    isPlaying = isPlaying,
                                    onPlay = { viewModel.playRecording(msg.filePath) },
                                    onStop = { viewModel.stopCurrentPlayback() },
                                    onDelete = { pendingDelete.value = msg },
                                    onEdit = { pendingEdit.value = msg },
                                    onToggleFavorite = { viewModel.toggleFavorite(msg) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditRecordingDialog(
    message: AudioMessage,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember(message.id) {
        mutableStateOf(
            message.title.ifBlank {
                message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
            }
        )
    }

    var transcript by remember(message.id) {
        mutableStateOf(message.transcript)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать запись") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = transcript,
                    onValueChange = { transcript = it },
                    label = { Text("Транскрипт") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = 280.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(title, transcript)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun RecordingCard(
    message: AudioMessage,
    fileExists: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = message.title.ifBlank { message.fileName },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = formatDate(message.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (message.isFavorite) {
                            Icons.Filled.Star
                        } else {
                            Icons.Filled.StarBorder
                        },
                        contentDescription = if (message.isFavorite) {
                            "Убрать из избранного"
                        } else {
                            "Добавить в избранное"
                        }
                    )
                }
            }

            Text(
                text = message.transcript.ifBlank { "Транскрипт пока пустой" },
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (fileExists) {
                    IconButton(
                        onClick = if (isPlaying) onStop else onPlay
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                Icons.Filled.Close
                            } else {
                                Icons.Rounded.PlayArrow
                            },
                            contentDescription = if (isPlaying) {
                                "Остановить"
                            } else {
                                "Воспроизвести"
                            }
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Файл не найден",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Редактировать"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить"
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}