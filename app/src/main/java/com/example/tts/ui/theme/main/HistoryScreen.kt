package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tts.data.model.AudioMessage
import com.example.tts.data.settings.HistorySortOption
import com.example.tts.ui.components.AppEmptyState
import com.example.tts.ui.components.AppScreenTopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (HistorySortOption) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onPlayClick: (String) -> Unit,
    onDeleteClick: (AudioMessage) -> Unit,
    onFavoriteClick: (AudioMessage) -> Unit,
    onUpdateMessage: (AudioMessage, String, String) -> Unit,
    onRetryTranscription: (AudioMessage) -> Unit,
    onExportTextClick: (AudioMessage) -> Unit,
    onExportAudioClick: (AudioMessage) -> Unit,
    onClearError: () -> Unit,
    onClearInfo: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onClearError()
    }

    LaunchedEffect(uiState.infoMessage) {
        val message = uiState.infoMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onClearInfo()
    }

    LaunchedEffect(uiState.sortOption, uiState.favoritesOnly) {
        if (listState.firstVisibleItemIndex > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppScreenTopBar(title = "История")
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HistoryFilterPanel(
                    searchQuery = uiState.searchQuery,
                    sortOption = uiState.sortOption,
                    favoritesOnly = uiState.favoritesOnly,
                    onSearchQueryChange = onSearchQueryChange,
                    onSortChange = onSortChange,
                    onToggleFavoritesOnly = onToggleFavoritesOnly
                )
            }

            if (uiState.isLoading) {
                item {
                    AppEmptyState(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        icon = Icons.Filled.Sync,
                        title = "Загрузка истории",
                        subtitle = "Подготавливаем список записей"
                    )
                }
            } else if (uiState.messages.isEmpty()) {
                item {
                    AppEmptyState(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        icon = if (uiState.searchQuery.isBlank()) {
                            Icons.Filled.History
                        } else {
                            Icons.Filled.Search
                        },
                        title = if (uiState.searchQuery.isBlank()) {
                            "История пока пустая"
                        } else {
                            "Ничего не найдено"
                        },
                        subtitle = if (uiState.searchQuery.isBlank()) {
                            "Сохрани первую голосовую заметку, и она появится здесь"
                        } else {
                            "Попробуй изменить запрос или отключить часть фильтров"
                        }
                    )
                }
            } else {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    HistoryMessageCard(
                        message = message,
                        isPlaying = uiState.currentlyPlayingPath == message.filePath,
                        onPlayClick = { onPlayClick(message.filePath) },
                        onDeleteClick = { onDeleteClick(message) },
                        onFavoriteClick = { onFavoriteClick(message) },
                        onUpdateMessage = { title, transcript ->
                            onUpdateMessage(message, title, transcript)
                        },
                        onRetryTranscription = { onRetryTranscription(message) },
                        onExportTextClick = { onExportTextClick(message) },
                        onExportAudioClick = { onExportAudioClick(message) },
                        onCopyTextClick = { copyTranscriptToClipboard(context, message) },
                        onShareTextClick = { shareTranscript(context, message) },
                        onShareAudioClick = { shareAudioFile(context, message) }
                    )
                }
            }
        }
    }
}

