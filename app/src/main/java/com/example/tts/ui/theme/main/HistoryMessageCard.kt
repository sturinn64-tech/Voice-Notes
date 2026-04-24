package com.example.tts.ui.theme.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.model.TranscriptionStatus
import com.example.tts.ui.components.AppSectionCard
import com.example.tts.ui.components.AppStatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryMessageCard(
    message: AudioMessage,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onUpdateMessage: (String, String) -> Unit,
    onRetryTranscription: () -> Unit,
    onExportTextClick: () -> Unit,
    onExportAudioClick: () -> Unit,
    onCopyTextClick: () -> Unit,
    onShareTextClick: () -> Unit,
    onShareAudioClick: () -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    val title = message.title.ifBlank {
        message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
    }

    val dateText = rememberSaveable(message.createdAt) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(message.createdAt))
    }

    AppSectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (message.isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = null
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Изменить") },
                        onClick = {
                            showMenu = false
                            showEditDialog = true
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Копировать текст") },
                        onClick = {
                            showMenu = false
                            onCopyTextClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Поделиться текстом") },
                        onClick = {
                            showMenu = false
                            onShareTextClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Поделиться аудио") },
                        onClick = {
                            showMenu = false
                            onShareAudioClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Экспорт текста") },
                        onClick = {
                            showMenu = false
                            onExportTextClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Экспорт аудио") },
                        onClick = {
                            showMenu = false
                            onExportAudioClick()
                        }
                    )

                    if (message.transcriptionStatus == TranscriptionStatus.ERROR) {
                        DropdownMenuItem(
                            text = { Text("Повторить распознавание") },
                            onClick = {
                                showMenu = false
                                onRetryTranscription()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Удалить") },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        AppStatusBadge(
            text = when (message.transcriptionStatus) {
                TranscriptionStatus.PROCESSING -> "Распознаётся"
                TranscriptionStatus.COMPLETED -> "Готово"
                TranscriptionStatus.ERROR -> "Ошибка"
                else -> "Без текста"
            }
        )

        Text(
            text = if (message.transcript.isBlank()) {
                "Транскрипт пока пустой"
            } else {
                message.transcript
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (message.transcript.isBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        FilledTonalButton(
            onClick = onPlayClick
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isPlaying) "Стоп" else "Воспроизвести")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись?") },
            text = { Text("Аудиофайл и запись в истории будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showEditDialog) {
        EditMessageDialog(
            initialTitle = title,
            initialTranscript = message.transcript,
            onDismiss = { showEditDialog = false },
            onConfirm = { newTitle, newTranscript ->
                showEditDialog = false
                onUpdateMessage(newTitle, newTranscript)
            }
        )
    }
}

@Composable
private fun EditMessageDialog(
    initialTitle: String,
    initialTranscript: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var transcript by rememberSaveable { mutableStateOf(initialTranscript) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактирование записи") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") }
                )

                OutlinedTextField(
                    value = transcript,
                    onValueChange = { transcript = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Текст") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, transcript) }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}