package com.example.tts.ui.theme.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.tts.data.settings.AppThemeMode
import com.example.tts.data.settings.HistorySortOption
import com.example.tts.ui.components.AppPillToggleButton
import com.example.tts.ui.components.AppScreenTopBar
import com.example.tts.ui.components.AppSectionCard
import com.example.tts.ui.components.AppSectionTitle


@OptIn(ExperimentalLayoutApi::class)
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
    onSignOut: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppScreenTopBar(title = "Настройки")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppSectionCard {
                AppSectionTitle(
                    title = "Аккаунт",
                    subtitle = "Данные текущего пользователя"
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsInfoRow(
                        icon = Icons.Filled.Email,
                        title = "Email",
                        subtitle = userEmail.ifBlank { "Email не указан" }
                    )

                    FilledTonalButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = null
                        )
                        Text(
                            text = "Выйти из аккаунта",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            AppSectionCard {
                AppSectionTitle(
                    title = "Внешний вид",
                    subtitle = "Оформление приложения"
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsInfoRow(
                        icon = Icons.Filled.Palette,
                        title = "Тема",
                        subtitle = when (themeMode) {
                            AppThemeMode.SYSTEM -> "Системная"
                            AppThemeMode.LIGHT -> "Светлая"
                            AppThemeMode.DARK -> "Тёмная"
                        }
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppPillToggleButton(
                            text = "Системная",
                            selected = themeMode == AppThemeMode.SYSTEM,
                            onClick = { onThemeModeChange(AppThemeMode.SYSTEM) }
                        )

                        AppPillToggleButton(
                            text = "Светлая",
                            selected = themeMode == AppThemeMode.LIGHT,
                            onClick = { onThemeModeChange(AppThemeMode.LIGHT) }
                        )

                        AppPillToggleButton(
                            text = "Тёмная",
                            selected = themeMode == AppThemeMode.DARK,
                            onClick = { onThemeModeChange(AppThemeMode.DARK) }
                        )
                    }
                }
            }

            AppSectionCard {
                AppSectionTitle(
                    title = "История",
                    subtitle = "Поведение записей и списка заметок"
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsSwitchRow(
                        icon = Icons.Filled.Delete,
                        title = "Подтверждение удаления",
                        subtitle = "Показывать диалог перед удалением записи",
                        checked = confirmDelete,
                        onCheckedChange = onConfirmDeleteChange
                    )

                    SettingsInfoRow(
                        icon = Icons.Filled.Sort,
                        title = "Сортировка по умолчанию",
                        subtitle = when (defaultSort) {
                            HistorySortOption.NEWEST -> "Сначала новые"
                            HistorySortOption.OLDEST -> "Сначала старые"
                            HistorySortOption.TITLE -> "По названию"
                        }
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppPillToggleButton(
                            text = "Новые",
                            selected = defaultSort == HistorySortOption.NEWEST,
                            onClick = { onDefaultSortChange(HistorySortOption.NEWEST) }
                        )

                        AppPillToggleButton(
                            text = "Старые",
                            selected = defaultSort == HistorySortOption.OLDEST,
                            onClick = { onDefaultSortChange(HistorySortOption.OLDEST) }
                        )

                        AppPillToggleButton(
                            text = "Название",
                            selected = defaultSort == HistorySortOption.TITLE,
                            onClick = { onDefaultSortChange(HistorySortOption.TITLE) }
                        )
                    }
                }
            }

            AppSectionCard {
                AppSectionTitle(
                    title = "О приложении",
                    subtitle = "Информация и описание"
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsInfoRow(
                        icon = Icons.Filled.Info,
                        title = "Версия",
                        subtitle = appVersion
                    )

                    FilledTonalButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null
                        )
                        Text(
                            text = "Открыть описание",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("О приложении")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Voice Notes помогает быстро записывать голосовые заметки и хранить их локально на устройстве.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Что умеет приложение:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "• запись голосовых заметок\n" +
                                "• сохранение истории записей\n" +
                                "• сортировка и поиск\n" +
                                "• избранное\n" +
                                "• редактирование текста заметки\n" +
                                "• экспорт и обмен файлами",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Версия: $appVersion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog = false }
                ) {
                    Text("Понятно")
                }
            }
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    SettingsRowContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsLeadingIcon(icon = icon)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    SettingsRowContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsLeadingIcon(icon = icon)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingsRowContainer(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsLeadingIcon(
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}