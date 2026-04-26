package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
    uiState: HistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (HistorySortOption) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onSelectedFolderChange: (String?) -> Unit,
    onSelectedTagChange: (String?) -> Unit,
    onToggleTrashMode: () -> Unit
) {
    var showFiltersSheet by rememberSaveable { mutableStateOf(false) }

    val hasActiveFilters =
        uiState.sortOption != HistorySortOption.NEWEST ||
                uiState.favoritesOnly ||
                !uiState.selectedFolder.isNullOrBlank() ||
                !uiState.selectedTag.isNullOrBlank()

    AppSectionCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                label = {
                    Text("Поиск")
                },
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        showFiltersSheet = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Фильтры",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onToggleTrashMode,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (uiState.isTrashMode) {
                            "История"
                        } else {
                            "Корзина"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (hasActiveFilters) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (uiState.sortOption != HistorySortOption.NEWEST) {
                        ActiveFilterChip(
                            text = uiState.sortOption.title,
                            onClick = {
                                onSortChange(HistorySortOption.NEWEST)
                            }
                        )
                    }

                    if (uiState.favoritesOnly) {
                        ActiveFilterChip(
                            text = "Избранное",
                            onClick = onToggleFavoritesOnly
                        )
                    }

                    uiState.selectedFolder?.let { folder ->
                        ActiveFilterChip(
                            text = "Папка: $folder",
                            onClick = {
                                onSelectedFolderChange(null)
                            }
                        )
                    }

                    uiState.selectedTag?.let { tag ->
                        ActiveFilterChip(
                            text = "#$tag",
                            onClick = {
                                onSelectedTagChange(null)
                            }
                        )
                    }
                }
            }

            Text(
                text = if (uiState.isTrashMode) {
                    "В корзине: ${uiState.messages.size}"
                } else {
                    "Найдено записей: ${uiState.messages.size}"
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
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Фильтры истории",
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Сортировка",
                        style = MaterialTheme.typography.titleSmall
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HistorySortOption.entries.forEach { sort ->
                            FilterChip(
                                selected = uiState.sortOption == sort,
                                onClick = {
                                    onSortChange(sort)
                                },
                                label = {
                                    Text(sort.title)
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Дополнительно",
                        style = MaterialTheme.typography.titleSmall
                    )

                    FilterChip(
                        selected = uiState.favoritesOnly,
                        onClick = onToggleFavoritesOnly,
                        label = {
                            Text("Только избранное")
                        }
                    )
                }

                if (!uiState.isTrashMode) {
                    HorizontalDivider()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Папки",
                            style = MaterialTheme.typography.titleSmall
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.selectedFolder == null,
                                onClick = {
                                    onSelectedFolderChange(null)
                                },
                                label = {
                                    Text("Все папки")
                                }
                            )

                            uiState.folders.forEach { folder ->
                                FilterChip(
                                    selected = uiState.selectedFolder == folder,
                                    onClick = {
                                        onSelectedFolderChange(folder)
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
                    }

                    HorizontalDivider()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Теги",
                            style = MaterialTheme.typography.titleSmall
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.selectedTag == null,
                                onClick = {
                                    onSelectedTagChange(null)
                                },
                                label = {
                                    Text("Все теги")
                                }
                            )

                            uiState.tags.forEach { tag ->
                                FilterChip(
                                    selected = uiState.selectedTag == tag,
                                    onClick = {
                                        onSelectedTagChange(tag)
                                    },
                                    label = {
                                        Text("#$tag")
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSearchQueryChange("")
                            onSortChange(HistorySortOption.NEWEST)

                            if (uiState.favoritesOnly) {
                                onToggleFavoritesOnly()
                            }

                            onSelectedFolderChange(null)
                            onSelectedTagChange(null)
                        }
                    ) {
                        Text("Сбросить")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showFiltersSheet = false
                        }
                    ) {
                        Text("Готово")
                    }
                }
            }
        }
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