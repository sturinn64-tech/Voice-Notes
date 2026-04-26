package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.model.Folder
import com.example.tts.data.model.Tag


@Composable
fun HistoryOrganizationDialog(
    message: AudioMessage,
    folders: List<Folder>,
    tags: List<Tag>,
    onDismiss: () -> Unit,
    onFolderSelected: (Long?) -> Unit,
    onAddTag: (Long) -> Unit,
    onRemoveTag: (Long) -> Unit
) {
    val title = message.title.ifBlank {
        message.fileName.substringBeforeLast(".").ifBlank { message.fileName }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Организация записи")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title)

                HorizontalDivider()

                Text("Папка")

                FolderPicker(
                    folders = folders,
                    selectedFolderId = message.folder?.id,
                    onSelected = onFolderSelected
                )

                HorizontalDivider()

                Text("Теги")

                if (tags.isEmpty()) {
                    Text("Тегов пока нет")
                } else {
                    tags.forEach { tag ->
                        val checked = message.tags.any { it.id == tag.id }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${tag.name}")

                            Switch(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        onAddTag(tag.id)
                                    } else {
                                        onRemoveTag(tag.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}

@Composable
private fun FolderPicker(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onSelected: (Long?) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val title = folders
        .firstOrNull { it.id == selectedFolderId }
        ?.name
        ?: "Без папки"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(title)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Без папки") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )

            folders.forEach { folder ->
                DropdownMenuItem(
                    text = { Text(folder.name) },
                    onClick = {
                        onSelected(folder.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CreateNameDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var value by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(label) }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(value)
                }
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}