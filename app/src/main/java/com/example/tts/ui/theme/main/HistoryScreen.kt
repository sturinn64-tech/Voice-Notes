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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.settings.HistorySortOption
import com.example.tts.ui.components.AppSectionCard
import java.io.File

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    confirmDelete: Boolean,

    onSearchQueryChange: (String) -> Unit,
    onSortChange: (HistorySortOption) -> Unit,
    onToggleFavoritesOnly: () -> Unit,

    onSelectedFolderChange: (String?) -> Unit,
    onSelectedTagChange: (String?) -> Unit,
    onToggleTrashMode: () -> Unit,

    onPlayClick: (String) -> Unit,
    onMoveToTrashClick: (AudioMessage) -> Unit,
    onRestoreClick: (AudioMessage) -> Unit,
    onDeleteForeverClick: (AudioMessage) -> Unit,
    onFavoriteClick: (AudioMessage) -> Unit,

    onUpdateMessage: (AudioMessage, String, String) -> Unit,
    onUpdateFolder: (AudioMessage, String) -> Unit,
    onAddTag: (AudioMessage, String) -> Unit,
    onRemoveTag: (AudioMessage, String) -> Unit,

    onRetryTranscription: (AudioMessage) -> Unit,
    onExportTextClick: (AudioMessage) -> Unit,
    onExportAudioClick: (AudioMessage) -> Unit,
    onEmptyTrashClick: () -> Unit,

    onClearError: () -> Unit,
    onClearInfo: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingMoveToTrash by remember { mutableStateOf<AudioMessage?>(null) }
    var pendingDeleteForever by remember { mutableStateOf<AudioMessage?>(null) }
    var pendingFolder by remember { mutableStateOf<AudioMessage?>(null) }
    var pendingAddTag by remember { mutableStateOf<AudioMessage?>(null) }
    var pendingRemoveTag by remember { mutableStateOf<AudioMessage?>(null) }
    var showEmptyTrashDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onClearError()
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onClearInfo()
        }
    }

    pendingMoveToTrash?.let { message ->
        ConfirmDialog(
            title = "Переместить в корзину?",
            text = "Запись останется в корзине, её можно будет восстановить.",
            confirmText = "В корзину",
            onConfirm = {
                onMoveToTrashClick(message)
                pendingMoveToTrash = null
            },
            onDismiss = {
                pendingMoveToTrash = null
            }
        )
    }

    pendingDeleteForever?.let { message ->
        ConfirmDialog(
            title = "Удалить навсегда?",
            text = "Запись и аудиофайл будут удалены окончательно.",
            confirmText = "Удалить",
            onConfirm = {
                onDeleteForeverClick(message)
                pendingDeleteForever = null
            },
            onDismiss = {
                pendingDeleteForever = null
            }
        )
    }

    if (showEmptyTrashDialog) {
        ConfirmDialog(
            title = "Очистить корзину?",
            text = "Все записи из корзины будут удалены навсегда.",
            confirmText = "Очистить",
            onConfirm = {
                onEmptyTrashClick()
                showEmptyTrashDialog = false
            },
            onDismiss = {
                showEmptyTrashDialog = false
            }
        )
    }

    pendingFolder?.let { message ->
        FolderDialog(
            message = message,
            onDismiss = {
                pendingFolder = null
            },
            onSave = { folderName ->
                onUpdateFolder(message, folderName)
                pendingFolder = null
            }
        )
    }

    pendingAddTag?.let { message ->
        AddTagDialog(
            message = message,
            onDismiss = {
                pendingAddTag = null
            },
            onSave = { tag ->
                onAddTag(message, tag)
                pendingAddTag = null
            }
        )
    }

    pendingRemoveTag?.let { message ->
        RemoveTagDialog(
            message = message,
            onDismiss = {
                pendingRemoveTag = null
            },
            onRemove = { tag ->
                onRemoveTag(message, tag)
                pendingRemoveTag = null
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 28.dp,
                        end = 28.dp,
                        top = 28.dp,
                        bottom = 28.dp
                    )
                ) {
                    item {
                        HistoryHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 700.dp),
                            title = if (uiState.isTrashMode) {
                                "Корзина"
                            } else {
                                "История"
                            },
                            showEmptyTrashButton = uiState.isTrashMode && uiState.messages.isNotEmpty(),
                            onEmptyTrashClick = {
                                if (confirmDelete) {
                                    showEmptyTrashDialog = true
                                } else {
                                    onEmptyTrashClick()
                                }
                            }
                        )
                    }

                    item {
                        HistoryFilterPanel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 700.dp),
                            uiState = uiState,
                            onSearchQueryChange = onSearchQueryChange,
                            onSortChange = onSortChange,
                            onToggleFavoritesOnly = onToggleFavoritesOnly,
                            onSelectedFolderChange = onSelectedFolderChange,
                            onSelectedTagChange = onSelectedTagChange,
                            onToggleTrashMode = onToggleTrashMode
                        )
                    }

                    if (uiState.messages.isEmpty()) {
                        item {
                            EmptyHistoryState(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 700.dp),
                                text = if (uiState.isTrashMode) {
                                    "Корзина пуста"
                                } else {
                                    "Записей пока нет"
                                }
                            )
                        }
                    } else {
                        items(
                            items = uiState.messages,
                            key = { it.id }
                        ) { message ->
                            val fileExists = File(message.filePath).exists()
                            val isPlaying = uiState.currentlyPlayingPath == message.filePath

                            HistoryMessageCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 700.dp),
                                message = message,
                                fileExists = fileExists,
                                isPlaying = isPlaying,
                                isTrashMode = uiState.isTrashMode,

                                onPlayClick = {
                                    onPlayClick(message.filePath)
                                },
                                onMoveToTrashClick = {
                                    if (confirmDelete) {
                                        pendingMoveToTrash = message
                                    } else {
                                        onMoveToTrashClick(message)
                                    }
                                },
                                onRestoreClick = {
                                    onRestoreClick(message)
                                },
                                onDeleteForeverClick = {
                                    if (confirmDelete) {
                                        pendingDeleteForever = message
                                    } else {
                                        onDeleteForeverClick(message)
                                    }
                                },
                                onFavoriteClick = {
                                    onFavoriteClick(message)
                                },

                                onUpdateMessage = { newTitle, newTranscript ->
                                    onUpdateMessage(message, newTitle, newTranscript)
                                },
                                onFolderClick = {
                                    pendingFolder = message
                                },
                                onAddTagClick = {
                                    pendingAddTag = message
                                },
                                onRemoveTagClick = {
                                    pendingRemoveTag = message
                                },

                                onRetryTranscription = {
                                    onRetryTranscription(message)
                                },
                                onExportTextClick = {
                                    onExportTextClick(message)
                                },
                                onExportAudioClick = {
                                    onExportAudioClick(message)
                                },
                                onCopyTextClick = {
                                    copyTranscript(
                                        context = context,
                                        text = message.transcript.ifBlank {
                                            "Транскрипт пустой"
                                        }
                                    )
                                },
                                onShareTextClick = {
                                    shareText(
                                        context = context,
                                        title = message.title.ifBlank { message.fileName },
                                        text = message.transcript.ifBlank {
                                            "Транскрипт пустой"
                                        }
                                    )
                                },
                                onShareAudioClick = {
                                    shareAudio(
                                        context = context,
                                        filePath = message.filePath,
                                        title = message.title.ifBlank { message.fileName }
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

@Composable
private fun HistoryHeader(
    modifier: Modifier = Modifier,
    title: String,
    showEmptyTrashButton: Boolean,
    onEmptyTrashClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (showEmptyTrashButton) {
            TextButton(onClick = onEmptyTrashClick) {
                Text("Очистить")
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    modifier: Modifier = Modifier,
    text: String
) {
    AppSectionCard(
        modifier = modifier,
        contentPadding = PaddingValues(28.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
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
private fun FolderDialog(
    message: AudioMessage,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var folderName by remember(message.id) {
        mutableStateOf(message.folder?.name.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Папка")
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = folderName,
                onValueChange = {
                    folderName = it
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                label = {
                    Text("Название папки")
                },
                placeholder = {
                    Text("Например: Учёба")
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(folderName)
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
private fun AddTagDialog(
    message: AudioMessage,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var tag by remember(message.id) {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Добавить тег")
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = tag,
                onValueChange = {
                    tag = it
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                label = {
                    Text("Тег")
                },
                placeholder = {
                    Text("Например: учёба")
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(tag)
                }
            ) {
                Text("Добавить")
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
private fun RemoveTagDialog(
    message: AudioMessage,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Удалить тег")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (message.tags.isEmpty()) {
                    Text("Тегов нет")
                } else {
                    message.tags.forEach { tag ->
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            onClick = {
                                onRemove(tag.name)
                            }
                        ) {
                            Text("#${tag.name}")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(text)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun copyTranscript(
    context: Context,
    text: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    clipboard.setPrimaryClip(
        ClipData.newPlainText("transcript", text)
    )

    Toast.makeText(
        context,
        "Текст скопирован",
        Toast.LENGTH_SHORT
    ).show()
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

        context.startActivity(
            Intent.createChooser(intent, "Поделиться текстом")
        )
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
            Toast.makeText(
                context,
                "Аудиофайл не найден",
                Toast.LENGTH_SHORT
            ).show()
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

        context.startActivity(
            Intent.createChooser(intent, "Поделиться аудио")
        )
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "Не удалось поделиться аудио",
            Toast.LENGTH_SHORT
        ).show()
    }
}

