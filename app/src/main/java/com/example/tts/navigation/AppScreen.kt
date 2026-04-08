package com.example.tts.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Record : AppScreen(
        route = "record",
        title = "Запись",
        icon = Icons.Filled.Mic
    )

    data object History : AppScreen(
        route = "history",
        title = "История",
        icon = Icons.Filled.History
    )

    data object Settings : AppScreen(
        route = "settings",
        title = "Настройки",
        icon = Icons.Filled.Settings
    )
}

val bottomBarScreens = listOf(
    AppScreen.Record,
    AppScreen.History,
    AppScreen.Settings
)