package com.example.tts.ui.theme.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.settings.HistorySortOption
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: MainUiState,
    onSignOut: () -> Unit,
    viewModel: MainViewModel,
    confirmDelete: Boolean,
    defaultSort: HistorySortOption
) {
    val context = LocalContext.current

    var pendingDelete by remember { mutableStateOf<AudioMessage?>(null) }
    var pendingEdit by remember { mutableStateOf<AudioMessage?>(null) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var selectedSort by remember(defaultSort) { mutableStateOf(defaultSort) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    pendingDelete?.let { msg ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Удалить запись?") },
            text = { Text("Запись и связанный аудиофайл будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecording(msg)
                        pendingDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    pendingEdit?.let { msg ->
        EditRecordingDialog(
            message = msg,
            onDismiss = { pendingEdit = null },
            onSave = { newTitle, newTranscript ->
                viewModel.updateMessage(
                    message = msg,
                    newTitle = newTitle,
                    newTranscript = newTranscript
                )
                pendingEdit = null
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
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
                        modifier = Modifier.fillMaxSize(),
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
                                HistorySortOption.NEWEST -> filtered.sortedWith(
                                    compareByDescending<AudioMessage> { it.isFavorite }
                                        .thenByDescending { it.createdAt }
                                )

                                HistorySortOption.OLDEST -> filtered.sortedWith(
                                    compareByDescending<AudioMessage> { it.isFavorite }
                                        .thenBy { it.createdAt }
                                )

                                HistorySortOption.TITLE -> filtered.sortedWith(
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
                            .imePadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 700.dp),
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Поиск и фильтры",
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    singleLine = true,
                                    label = { Text("Поиск") },
                                    placeholder = { Text("По названию и тексту") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text
                                    )
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
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Switch(
                                            checked = favoritesOnly,
                                            onCheckedChange = { favoritesOnly = it }
                                        )
                                    }

                                    Box {
                                        OutlinedButton(
                                            onClick = { sortMenuExpanded = true },
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(selectedSort.title)
                                        }

                                        DropdownMenu(
                                            expanded = sortMenuExpanded,
                                            onDismissRequest = { sortMenuExpanded = false }
                                        ) {
                                            HistorySortOption.entries.forEach { sort ->
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

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                Text(
                                    text = "Найдено записей: ${filteredMessages.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        when {
                            uiState.messages.isEmpty() -> {
                                EmptyHistoryState(
                                    text = "Записей пока нет"
                                )
                            }

                            filteredMessages.isEmpty() -> {
                                EmptyHistoryState(
                                    text = "Ничего не найдено по текущим фильтрам"
                                )
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .widthIn(max = 700.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            onDelete = {
                                                if (confirmDelete) {
                                                    pendingDelete = msg
                                                } else {
                                                    viewModel.deleteRecording(msg)
                                                }
                                            },
                                            onEdit = { pendingEdit = msg },
                                            onToggleFavorite = { viewModel.toggleFavorite(msg) },
                                            onCopyTranscript = {
                                                copyTranscript(
                                                    context = context,
                                                    text = msg.transcript.ifBlank {
                                                        "Транскрипт пустой"
                                                    }
                                                )
                                            },
                                            onShareTranscript = {
                                                shareText(
                                                    context = context,
                                                    title = msg.title.ifBlank { msg.fileName },
                                                    text = msg.transcript.ifBlank {
                                                        "Транскрипт пустой"
                                                    }
                                                )
                                            },
                                            onShareAudio = {
                                                shareAudio(
                                                    context = context,
                                                    filePath = msg.filePath,
                                                    title = msg.title.ifBlank { msg.fileName }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    text: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    var transcript by remember(message.id) { mutableStateOf(message.transcript) }

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Название") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = transcript,
                    onValueChange = { transcript = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = 280.dp),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Транскрипт") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, transcript) }
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
    onToggleFavorite: () -> Unit,
    onCopyTranscript: () -> Unit,
    onShareTranscript: () -> Unit,
    onShareAudio: () -> Unit
) {
    var shareMenuExpanded by remember(message.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = message.title.ifBlank { message.fileName },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(
                            text = formatDate(message.createdAt),
                            isAccent = false
                        )

                        if (message.isFavorite) {
                            StatusPill(
                                text = "Избранное",
                                isAccent = true
                            )
                        }
                    }
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
                        },
                        tint = if (message.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Text(
                text = message.transcript.ifBlank { "Транскрипт пока пустой" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (fileExists) {
                    IconButton(
                        onClick = if (isPlaying) onStop else onPlay
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                Icons.Filled.Stop
                            } else {
                                Icons.Filled.PlayArrow
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
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                IconButton(onClick = onCopyTranscript) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Скопировать текст"
                    )
                }

                Box {
                    IconButton(onClick = { shareMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Поделиться"
                        )
                    }

                    DropdownMenu(
                        expanded = shareMenuExpanded,
                        onDismissRequest = { shareMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Поделиться текстом") },
                            onClick = {
                                shareMenuExpanded = false
                                onShareTranscript()
                            }
                        )

                        if (fileExists) {
                            DropdownMenuItem(
                                text = { Text("Поделиться аудио") },
                                onClick = {
                                    shareMenuExpanded = false
                                    onShareAudio()
                                }
                            )
                        }
                    }
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

@Composable
private fun StatusPill(
    text: String,
    isAccent: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isAccent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isAccent) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun copyTranscript(
    context: Context,
    text: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("transcript", text))
    Toast.makeText(context, "Текст скопирован", Toast.LENGTH_SHORT).show()
}

private fun shareText(
    context: Context,
    title: String,
    text: String
) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться текстом"))
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "Не удалось открыть меню отправки",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun shareAudio(
    context: Context,
    filePath: String,
    title: String
) {
    try {
        val file = File(filePath)

        if (!file.exists()) {
            Toast.makeText(context, "Аудиофайл не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Поделиться аудио"))
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "Не удалось поделиться аудио",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}