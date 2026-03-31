package com.example.tts.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Record: AppScreen(
        route = "record",
        title = "Запись",
        icon = Icons.Filled.Mic
    )

    data object History : AppScreen(
        route = "history",
        title = "История",
        icon = Icons.Filled.History
    )
}

val bottomBarScreens = listOf(
    AppScreen.Record,
    AppScreen.History
)