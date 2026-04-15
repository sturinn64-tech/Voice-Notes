package com.example.tts.ui.theme.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tts.data.settings.AppThemeMode
import com.example.tts.data.settings.HistorySortOption


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userEmail: String,
    themeMode: AppThemeMode,
    confirmDelete: Boolean,
    defaultSort: HistorySortOption,
    appVersion: String,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onConfirmDeleteChange: (Boolean) -> Unit,
    onDefaultSortChange: (HistorySortOption) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSectionCard(
                title = "Аккаунт"
            ) {
                SettingsInfoRow(
                    icon = Icons.Filled.AccountCircle,
                    title = "Email",
                    subtitle = userEmail.ifBlank { "Почта не найдена" }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                SettingsActionRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Выйти из аккаунта",
                    subtitle = "Завершить текущую сессию",
                    onClick = onSignOut,
                    isDanger = true
                )
            }

            SettingsSectionCard(
                title = "Поведение приложения"
            ) {
                SettingsActionWithButtonRow(
                    icon = Icons.Filled.Palette,
                    title = "Тема",
                    subtitle = themeMode.toDisplayName(),
                    buttonText = "Изменить",
                    onClick = { showThemeDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                SettingsSwitchRow(
                    icon = Icons.Filled.Delete,
                    title = "Подтверждение удаления",
                    subtitle = "Показывать диалог перед удалением записи",
                    checked = confirmDelete,
                    onCheckedChange = onConfirmDeleteChange
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                SettingsActionWithButtonRow(
                    icon = Icons.Filled.Sort,
                    title = "Сортировка истории",
                    subtitle = defaultSort.toDisplayName(),
                    buttonText = "Изменить",
                    onClick = { showSortDialog = true }
                )
            }

            SettingsSectionCard(
                title = "О приложении"
            ) {
                SettingsActionRow(
                    icon = Icons.Filled.Info,
                    title = "Информация",
                    subtitle = "Описание приложения, функции и версия",
                    onClick = { showAboutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            selectedMode = themeMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = {
                onThemeModeChange(it)
                showThemeDialog = false
            }
        )
    }

    if (showSortDialog) {
        SortOptionDialog(
            selectedSort = defaultSort,
            onDismiss = { showSortDialog = false },
            onSortSelected = {
                onDefaultSortChange(it)
                showSortDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(
            appVersion = appVersion,
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SettingsIconBox(
    icon: ImageVector,
    isDanger: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = if (isDanger) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDanger) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon = icon)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isDanger) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(
            icon = icon,
            isDanger = isDanger
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDanger) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsActionWithButtonRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon = icon)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.height(48.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Text(
                text = buttonText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon = icon)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ThemeModeDialog(
    selectedMode: AppThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit
) {
    val options = listOf(
        AppThemeMode.SYSTEM,
        AppThemeMode.LIGHT,
        AppThemeMode.DARK
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Тема приложения") },
        text = {
            Column {
                options.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMode == mode,
                            onClick = { onThemeSelected(mode) }
                        )
                        Text(text = mode.toDisplayName())
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
private fun SortOptionDialog(
    selectedSort: HistorySortOption,
    onDismiss: () -> Unit,
    onSortSelected: (HistorySortOption) -> Unit
) {
    val options = listOf(
        HistorySortOption.NEWEST,
        HistorySortOption.OLDEST,
        HistorySortOption.TITLE
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка истории") },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSort == option,
                            onClick = { onSortSelected(option) }
                        )
                        Text(text = option.toDisplayName())
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
private fun AboutAppDialog(
    appVersion: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("О приложении")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Voice Notes — приложение для записи, хранения и организации голосовых заметок."
                )
                Text(
                    text = "Основные возможности:"
                )
                Text(
                    text = "• запись аудио\n" +
                            "• локальное хранение записей\n" +
                            "• история записей\n" +
                            "• поиск и сортировка\n" +
                            "• избранное и редактирование\n" +
                            "• офлайн-транскрибация"
                )
                Text(
                    text = "Версия: $appVersion",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Понятно")
            }
        }
    )
}

private fun AppThemeMode.toDisplayName(): String {
    return when (this) {
        AppThemeMode.SYSTEM -> "Как в системе"
        AppThemeMode.LIGHT -> "Светлая"
        AppThemeMode.DARK -> "Тёмная"
    }
}

private fun HistorySortOption.toDisplayName(): String {
    return when (this) {
        HistorySortOption.NEWEST -> "Сначала новые"
        HistorySortOption.OLDEST -> "Сначала старые"
        HistorySortOption.TITLE -> "По названию"
    }
}