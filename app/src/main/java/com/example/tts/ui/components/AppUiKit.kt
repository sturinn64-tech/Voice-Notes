package com.example.tts.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private object AppUiTokens {
    val CardRadius = 24.dp
    val InnerCardRadius = 18.dp
    val PillRadius = 16.dp
    val BadgeRadius = 14.dp

    val CardPadding = PaddingValues(16.dp)
    val ItemPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    val PillPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)

    val IconContainerSize = 48.dp
    val BadgeHorizontal = 12.dp
    val BadgeVertical = 7.dp

    const val CardBorderAlpha = 0.22f
    const val CardElevation = 3f
    const val SelectedFillAlpha = 0.18f
    const val SelectedBorderAlpha = 0.34f
    const val UnselectedBorderAlpha = 0.32f
    const val BadgeFillAlpha = 0.16f
    const val BadgeBorderAlpha = 0.28f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreenTopBar(
    title: String
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun AppSectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = AppUiTokens.CardPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppUiTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppUiTokens.CardBorderAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppUiTokens.CardElevation.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun AppSectionTitle(
    title: String,
    subtitle: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppPillToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = AppUiTokens.SelectedFillAlpha)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = AppUiTokens.SelectedBorderAlpha)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppUiTokens.UnselectedBorderAlpha)
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 42.dp),
        shape = RoundedCornerShape(AppUiTokens.PillRadius),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = AppUiTokens.PillPadding
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun AppStatusBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppUiTokens.BadgeRadius),
        color = MaterialTheme.colorScheme.primary.copy(alpha = AppUiTokens.BadgeFillAlpha),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = AppUiTokens.BadgeBorderAlpha)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = AppUiTokens.BadgeHorizontal,
                vertical = AppUiTokens.BadgeVertical
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AppEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    AppSectionCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(AppUiTokens.InnerCardRadius),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}