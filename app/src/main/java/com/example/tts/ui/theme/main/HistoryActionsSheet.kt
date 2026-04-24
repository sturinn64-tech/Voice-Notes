package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryActionsSheet(
    title: String,
    isPlaying: Boolean,
    showRetryAction: Boolean,
    onDismissRequest: () -> Unit,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    onCopyTextClick: () -> Unit,
    onShareTextClick: () -> Unit,
    onShareAudioClick: () -> Unit,
    onExportTextClick: () -> Unit,
    onExportAudioClick: () -> Unit,
    onRetryTranscriptionClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Действия с записью",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            HistoryActionSheetItem(
                icon = Icons.Filled.Edit,
                title = "Изменить",
                onClick = {
                    onDismissRequest()
                    onEditClick()
                }
            )

            HistoryActionSheetItem(
                icon = Icons.Filled.ContentCopy,
                title = "Копировать текст",
                onClick = {
                    onDismissRequest()
                    onCopyTextClick()
                }
            )

            HistoryActionSheetItem(
                icon = Icons.Filled.Description,
                title = "Поделиться текстом",
                onClick = {
                    onDismissRequest()
                    onShareTextClick()
                }
            )

            HistoryActionSheetItem(
                icon = Icons.Filled.Share,
                title = "Поделиться аудио",
                onClick = {
                    onDismissRequest()
                    onShareAudioClick()
                }
            )

            HistoryActionSheetItem(
                icon = Icons.Filled.Download,
                title = "Экспортировать текст",
                onClick = {
                    onDismissRequest()
                    onExportTextClick()
                }
            )

            HistoryActionSheetItem(
                icon = Icons.Filled.Audiotrack,
                title = "Экспортировать аудио",
                onClick = {
                    onDismissRequest()
                    onExportAudioClick()
                }
            )

            if (showRetryAction) {
                HistoryActionSheetItem(
                    icon = Icons.Filled.Refresh,
                    title = "Повторить распознавание",
                    onClick = {
                        onDismissRequest()
                        onRetryTranscriptionClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            HistoryActionSheetItem(
                icon = Icons.Filled.Delete,
                title = "Удалить",
                isDestructive = true,
                onClick = {
                    onDismissRequest()
                    onDeleteClick()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HistoryActionSheetItem(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}