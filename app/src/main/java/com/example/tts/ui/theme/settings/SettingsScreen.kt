package com.example.tts.ui.theme.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.tts.data.settings.AppSettings
import com.example.tts.data.settings.AppThemeMode
import com.example.tts.data.settings.HistorySortOption
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: FirebaseUser,
    settings: AppSettings,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onConfirmDeleteChanged: (Boolean) -> Unit,
    onDefaultSortChanged: (HistorySortOption) -> Unit,
    onSignOut: () -> Unit
) {
    val themeMenuExpanded = remember { mutableStateOf(false) }
    val sortMenuExpanded = remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Настройки") }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SettingsSectionCard(title = "Аккаунт") {
                    SettingsInfoRow(
                        icon = Icons.Filled.Person,
                        title = "Email",
                        value = user.email ?: "Email недоступен",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.Person,
                        title = "UID",
                        value = user.uid,
                        showDivider = false
                    )
                }

                SettingsSectionCard(title = "Поведение приложения") {
                    SettingDropdownRow(
                        icon = Icons.Filled.Palette,
                        title = "Тема",
                        value = settings.themeMode.toLabel(),
                        expanded = themeMenuExpanded.value,
                        onExpand = { themeMenuExpanded.value = true },
                        onDismiss = { themeMenuExpanded.value = false }
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.toLabel()) },
                                onClick = {
                                    themeMenuExpanded.value = false
                                    onThemeModeSelected(mode)
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingToggleRow(
                        icon = Icons.Filled.DeleteOutline,
                        title = "Подтверждение удаления",
                        subtitle = "Показывать диалог перед удалением записи",
                        checked = settings.confirmDelete,
                        onCheckedChange = onConfirmDeleteChanged
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingDropdownRow(
                        icon = Icons.Filled.Sort,
                        title = "Сортировка истории",
                        value = settings.defaultHistorySort.title,
                        expanded = sortMenuExpanded.value,
                        onExpand = { sortMenuExpanded.value = true },
                        onDismiss = { sortMenuExpanded.value = false }
                    ) {
                        HistorySortOption.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.title) },
                                onClick = {
                                    sortMenuExpanded.value = false
                                    onDefaultSortChanged(sort)
                                }
                            )
                        }
                    }
                }

                SettingsSectionCard(title = "Хранение") {
                    SettingsInfoRow(
                        icon = Icons.Filled.Storage,
                        title = "Аудиофайлы",
                        value = "Хранятся локально на устройстве",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.Storage,
                        title = "Настройки",
                        value = "Сохраняются локально через DataStore",
                        showDivider = false
                    )
                }

                SettingsSectionCard(title = "О приложении") {
                    SettingsInfoRow(
                        icon = Icons.Filled.Info,
                        title = "Название",
                        value = "Voice Notes",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.Info,
                        title = "Стек",
                        value = "Jetpack Compose, Room, Firebase Auth, Vosk, DataStore",
                        showDivider = false
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 700.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Выход из аккаунта",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = "Вернет на экран авторизации.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Button(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null
                            )
                            Text(
                                text = "Выйти",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 700.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            content()
        }
    }
}

@Composable
private fun SettingsInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    showDivider: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ) {
                Box(
                    modifier = Modifier.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
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

@Composable
private fun SettingDropdownRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    menuContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ) {
            Box(
                modifier = Modifier.padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            OutlinedButton(
                onClick = onExpand,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Изменить")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss
            ) {
                menuContent()
            }
        }
    }
}

private fun AppThemeMode.toLabel(): String {
    return when (this) {
        AppThemeMode.SYSTEM -> "Как в системе"
        AppThemeMode.LIGHT -> "Светлая"
        AppThemeMode.DARK -> "Темная"
    }
}