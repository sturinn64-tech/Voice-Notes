package com.example.tts.data.settings

data class AppSettings (
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val confirmDelete: Boolean = true,
    val defaultHistorySort: HistorySortOption = HistorySortOption.NEWEST
)
