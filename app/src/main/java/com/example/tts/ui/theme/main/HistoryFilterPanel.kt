package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tts.data.settings.HistorySortOption
import com.example.tts.ui.components.AppSectionCard

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun HistoryFilterPanel(
    modifier: Modifier = Modifier,

    searchQuery: String,
    sortOption: HistorySortOption,
    favoritesOnly: Boolean,

    folders: List<String>,
    tags: List<String>,
    selectedFolder: String?,
    selectedTag: String?,
    isTrashMode: Boolean,

    onSearchQueryChange: (String) -> Unit,
    onSortChange: (HistorySortOption) -> Unit,
    onToggleFavoritesOnly: () -> Unit,

    onCreateFolder: (String) -> Unit,
    onFolderSelected: (String?) -> Unit,
    onTagSelected: (String?) -> Unit,
    onToggleTrashMode: () -> Unit
) {
    var showFiltersSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showCreateFolderDialog by rememberSaveable { mutableStateOf(false) }
    var newFolderName by rememberSaveable { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val cleanFolders = folders
        .filter { it.isNotBlank() }
        .distinct()

    val cleanTags = tags
        .filter { it.isNotBlank() }
        .distinct()

    val hasActiveFilters =
        favoritesOnly ||
                !selectedFolder.isNullOrBlank() ||
                !selectedTag.isNullOrBlank() ||
                sortOption != HistorySortOption.NEWEST

    AppSectionCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                label = {
                    Text("Поиск")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        showFiltersSheet = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Фильтры")
                }

                OutlinedButton(
                    onClick = onToggleTrashMode,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (isTrashMode) {
                            "История"
                        } else {
                            "Корзина"
                        }
                    )
                }
            }

            if (hasActiveFilters) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (sortOption != HistorySortOption.NEWEST) {
                        ActiveFilterChip(
                            text = sortOption.titleText(),
                            onClick = {
                                onSortChange(HistorySortOption.NEWEST)
                            }
                        )
                    }

                    if (favoritesOnly) {
                        ActiveFilterChip(
                            text = "Избранное",
                            onClick = onToggleFavoritesOnly
                        )
                    }

                    if (!selectedFolder.isNullOrBlank()) {
                        ActiveFilterChip(
                            text = "Папка: $selectedFolder",
                            onClick = {
                                onFolderSelected(null)
                            }
                        )
                    }

                    if (!selectedTag.isNullOrBlank()) {
                        ActiveFilterChip(
                            text = "#$selectedTag",
                            onClick = {
                                onTagSelected(null)
                            }
                        )
                    }
                }
            }

            Text(
                text = if (isTrashMode) {
                    "В корзине: ${folders.size.coerceAtLeast(0)}"
                } else {
                    "Фильтры применяются к истории записей"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showFiltersSheet = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 28.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Фильтры истории",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Сортировка",
                        style = MaterialTheme.typography.titleMedium
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortOption == HistorySortOption.NEWEST,
                            onClick = {
                                onSortChange(HistorySortOption.NEWEST)
                            },
                            label = {
                                Text("Сначала новые")
                            }
                        )

                        FilterChip(
                            selected = sortOption == HistorySortOption.OLDEST,
                            onClick = {
                                onSortChange(HistorySortOption.OLDEST)
                            },
                            label = {
                                Text("Сначала старые")
                            }
                        )

                        FilterChip(
                            selected = sortOption == HistorySortOption.TITLE,
                            onClick = {
                                onSortChange(HistorySortOption.TITLE)
                            },
                            label = {
                                Text("По названию")
                            }
                        )
                    }
                }

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Дополнительно",
                        style = MaterialTheme.typography.titleMedium
                    )

                    FilterChip(
                        selected = favoritesOnly,
                        onClick = onToggleFavoritesOnly,
                        label = {
                            Text("Только избранное")
                        }
                    )
                }

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Папки",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick = {
                            showCreateFolderDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Создать папку")
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFolder == null,
                            onClick = {
                                onFolderSelected(null)
                            },
                            label = {
                                Text("Все папки")
                            }
                        )

                        cleanFolders.forEach { folder ->
                            FilterChip(
                                selected = selectedFolder == folder,
                                onClick = {
                                    onFolderSelected(folder)
                                },
                                label = {
                                    Text(
                                        text = folder,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    if (cleanFolders.isEmpty()) {
                        Text(
                            text = "Папок пока нет",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Теги",
                        style = MaterialTheme.typography.titleMedium
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = {
                                onTagSelected(null)
                            },
                            label = {
                                Text("Все теги")
                            }
                        )

                        cleanTags.forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = {
                                    onTagSelected(tag)
                                },
                                label = {
                                    Text(
                                        text = "#$tag",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    if (cleanTags.isEmpty()) {
                        Text(
                            text = "Тегов пока нет",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSearchQueryChange("")
                            onSortChange(HistorySortOption.NEWEST)

                            if (favoritesOnly) {
                                onToggleFavoritesOnly()
                            }

                            onFolderSelected(null)
                            onTagSelected(null)
                        }
                    ) {
                        Text("Сбросить")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showFiltersSheet = false
                        },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Готово")
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateFolderDialog = false
                newFolderName = ""
            },
            title = {
                Text("Новая папка")
            },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Название папки") },
                    placeholder = { Text("Например: Учёба") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newFolderName.trim().isNotBlank(),
                    onClick = {
                        val safeName = newFolderName.trim()

                        if (safeName.isNotBlank()) {
                            onCreateFolder(safeName)
                            newFolderName = ""
                            showCreateFolderDialog = false
                        }
                    }
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateFolderDialog = false
                        newFolderName = ""
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ActiveFilterChip(
    text: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

private fun HistorySortOption.titleText(): String {
    return when (this) {
        HistorySortOption.NEWEST -> "Сначала новые"
        HistorySortOption.OLDEST -> "Сначала старые"
        HistorySortOption.TITLE -> "По названию"
    }
}