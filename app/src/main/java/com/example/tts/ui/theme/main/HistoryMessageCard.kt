package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryMessageCard(
    modifier: Modifier = Modifier,
    message: AudioMessage,
    fileExists: Boolean,
    isPlaying: Boolean,
    isTrashMode: Boolean,

    onPlayClick: () -> Unit,
    onMoveToTrashClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteForeverClick: () -> Unit,
    onFavoriteClick: () -> Unit,

    onUpdateMessage: (String, String) -> Unit,
    onFolderClick: () -> Unit,
    onAddTagClick: () -> Unit,
    onRemoveTagClick: () -> Unit,

    onRetryTranscription: () -> Unit,
    onExportTextClick: () -> Unit,
    onExportAudioClick: () -> Unit,
    onCopyTextClick: () -> Unit,
    onShareTextClick: () -> Unit,
    onShareAudioClick: () -> Unit
) {
    var showMenu by rememberSaveable(message.id) { mutableStateOf(false) }
    var showEditDialog by rememberSaveable(message.id) { mutableStateOf(false) }

    val title = message.title.ifBlank {
        message.fileName.substringBeforeLast(".").ifBlank {
            message.fileName
        }
    }

    val dateText = remember(message.createdAt) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(message.createdAt))
    }

    AppSectionCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isTrashMode) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (message.isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = "Избранное",
                        tint = if (message.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Меню"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = {
                        showMenu = false
                    }
                ) {
                    if (isTrashMode) {
                        DropdownMenuItem(
                            text = { Text("Восстановить") },
                            onClick = {
                                showMenu = false
                                onRestoreClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Удалить навсегда") },
                            onClick = {
                                showMenu = false
                                onDeleteForeverClick()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Изменить") },
                            onClick = {
                                showMenu = false
                                showEditDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Изменить папку") },
                            onClick = {
                                showMenu = false
                                onFolderClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Добавить тег") },
                            onClick = {
                                showMenu = false
                                onAddTagClick()
                            }
                        )

                        if (message.tags.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Удалить тег") },
                                onClick = {
                                    showMenu = false
                                    onRemoveTagClick()
                                }
                            )
                        }

                        HorizontalDivider()

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

                        if (fileExists) {
                            DropdownMenuItem(
                                text = { Text("Поделиться аудио") },
                                onClick = {
                                    showMenu = false
                                    onShareAudioClick()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Экспорт текста") },
                            onClick = {
                                showMenu = false
                                onExportTextClick()
                            }
                        )

                        if (fileExists) {
                            DropdownMenuItem(
                                text = { Text("Экспорт аудио") },
                                onClick = {
                                    showMenu = false
                                    onExportAudioClick()
                                }
                            )
                        }

                        if (message.transcriptionStatus == TranscriptionStatus.ERROR) {
                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Повторить распознавание") },
                                onClick = {
                                    showMenu = false
                                    onRetryTranscription()
                                }
                            )
                        }

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("В корзину") },
                            onClick = {
                                showMenu = false
                                onMoveToTrashClick()
                            }
                        )
                    }
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AppStatusBadge(
                text = when (message.transcriptionStatus) {
                    TranscriptionStatus.PROCESSING -> "Распознаётся"
                    TranscriptionStatus.COMPLETED -> "Готово"
                    TranscriptionStatus.ERROR -> "Ошибка"
                    TranscriptionStatus.EMPTY -> "Без текста"
                }
            )

            val folderName = message.folder?.name.orEmpty()

            if (folderName.isNotBlank()) {
                AssistChip(
                    onClick = onFolderClick,
                    label = {
                        Text(
                            text = "Папка: $folderName",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }

            if (!fileExists && !isTrashMode) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text("Файл не найден")
                    }
                )
            }
        }

        if (message.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.tags.take(3).forEach { tag ->
                    AssistChip(
                        onClick = onRemoveTagClick,
                        label = {
                            Text("#${tag.name}")
                        }
                    )
                }

                if (message.tags.size > 3) {
                    AssistChip(
                        onClick = onRemoveTagClick,
                        label = {
                            Text("+${message.tags.size - 3}")
                        }
                    )
                }
            }
        }

        Text(
            text = message.transcript.ifBlank {
                "Транскрипт пока пустой"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (message.transcript.isBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        if (message.transcriptionStatus == TranscriptionStatus.ERROR &&
            !message.transcriptionError.isNullOrBlank()
        ) {
            Text(
                text = message.transcriptionError.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        if (isTrashMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRestoreClick) {
                    Text("Восстановить")
                }

                TextButton(onClick = onDeleteForeverClick) {
                    Text("Удалить")
                }
            }
        } else {
            FilledTonalButton(
                onClick = onPlayClick,
                enabled = fileExists
            ) {
                Icon(
                    imageVector = if (isPlaying) {
                        Icons.Filled.Stop
                    } else {
                        Icons.Filled.PlayArrow
                    },
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isPlaying) {
                        "Стоп"
                    } else {
                        "Воспроизвести"
                    }
                )
            }
        }
    }

    if (showEditDialog) {
        EditMessageDialog(
            initialTitle = title,
            initialTranscript = message.transcript,
            onDismiss = {
                showEditDialog = false
            },
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
        title = {
            Text("Редактирование записи")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    label = {
                        Text("Название")
                    }
                )

                OutlinedTextField(
                    value = transcript,
                    onValueChange = {
                        transcript = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = {
                        Text("Текст")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, transcript)
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