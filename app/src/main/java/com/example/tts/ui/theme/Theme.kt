package com.example.tts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.tts.data.settings.AppThemeMode

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandTextPrimary,

    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrandSecondaryContainer,
    onSecondaryContainer = BrandTextPrimary,

    tertiary = BrandTertiary,
    onTertiary = Color.White,

    background = AppBackgroundLight,
    onBackground = BrandTextPrimary,

    surface = AppSurfaceLight,
    onSurface = BrandTextPrimary,

    surfaceVariant = AppSurfaceAltLight,
    onSurfaceVariant = BrandTextSecondary,

    outline = BrandOutline,
    outlineVariant = BrandOutlineVariant,

    error = BrandError,
    onError = Color.White,
    errorContainer = BrandErrorContainer,
    onErrorContainer = BrandTextPrimary
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFF111827),
    primaryContainer = Color(0xFF2A326A),
    onPrimaryContainer = BrandTextPrimaryDark,

    secondary = BrandSecondaryDark,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF163B4A),
    onSecondaryContainer = BrandTextPrimaryDark,

    tertiary = BrandTertiaryDark,
    onTertiary = Color(0xFF0F172A),

    background = AppBackgroundDark,
    onBackground = BrandTextPrimaryDark,

    surface = AppSurfaceDark,
    onSurface = BrandTextPrimaryDark,

    surfaceVariant = AppSurfaceAltDark,
    onSurfaceVariant = BrandTextSecondaryDark,

    outline = BrandOutlineDark,
    outlineVariant = BrandOutlineVariantDark,

    error = BrandErrorDark,
    onError = Color(0xFF1F1111),
    errorContainer = BrandErrorContainerDark,
    onErrorContainer = BrandTextPrimaryDark
)

@Composable
fun TtsTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}