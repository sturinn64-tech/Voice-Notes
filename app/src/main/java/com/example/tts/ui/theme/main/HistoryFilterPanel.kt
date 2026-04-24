package com.example.tts.ui.theme.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tts.data.settings.HistorySortOption
import com.example.tts.ui.components.AppPillToggleButton
import com.example.tts.ui.components.AppSectionCard
import com.example.tts.ui.components.AppSectionTitle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryFilterPanel(
    searchQuery: String,
    sortOption: HistorySortOption,
    favoritesOnly: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (HistorySortOption) -> Unit,
    onToggleFavoritesOnly: () -> Unit
) {
    AppSectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        AppSectionTitle(title = "Фильтры")

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Поиск") }
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppPillToggleButton(
                text = "Новые",
                selected = sortOption == HistorySortOption.NEWEST,
                onClick = { onSortChange(HistorySortOption.NEWEST) }
            )

            AppPillToggleButton(
                text = "Старые",
                selected = sortOption == HistorySortOption.OLDEST,
                onClick = { onSortChange(HistorySortOption.OLDEST) }
            )

            AppPillToggleButton(
                text = "Название",
                selected = sortOption == HistorySortOption.TITLE,
                onClick = { onSortChange(HistorySortOption.TITLE) }
            )

            AppPillToggleButton(
                text = "Избранное",
                selected = favoritesOnly,
                onClick = onToggleFavoritesOnly,
                leadingIcon = if (favoritesOnly) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Filled.FavoriteBorder
                }
            )
        }
    }
}
