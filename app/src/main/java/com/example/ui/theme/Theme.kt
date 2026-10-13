package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private fun createColorSchemeFor(theme: AppTheme) = darkColorScheme(
    primary = theme.primaryLight,
    onPrimary = theme.background,
    primaryContainer = theme.primary,
    onPrimaryContainer = Color.White,
    secondary = theme.accent,
    onSecondary = theme.background,
    secondaryContainer = theme.surfaceCardElevated,
    onSecondaryContainer = theme.accent,
    tertiary = theme.primaryGlow,
    onTertiary = theme.background,
    background = theme.background,
    onBackground = TextPrimary,
    surface = theme.surface,
    onSurface = TextPrimary,
    surfaceVariant = theme.surfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = theme.surfaceBorder,
    outlineVariant = theme.surfaceCardElevated
)

private val HomiesShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.ELECTRIC_PURPLE,
    content: @Composable () -> Unit
) {
    val colorScheme = createColorSchemeFor(appTheme)

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = HomiesShapes,
            typography = Typography,
            content = content
        )
    }
}
