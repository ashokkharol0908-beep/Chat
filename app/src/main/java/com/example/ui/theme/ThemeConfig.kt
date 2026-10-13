package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val primary: Color,
    val primaryLight: Color,
    val primaryGlow: Color,
    val background: Color,
    val surface: Color,
    val surfaceCard: Color,
    val surfaceCardElevated: Color,
    val surfaceBorder: Color,
    val sentBubble: Color,
    val receivedBubble: Color,
    val accent: Color
) {
    ELECTRIC_PURPLE(
        id = "electric_purple",
        title = "Electric Cyber",
        subtitle = "Deep black with electric purple & cyan",
        icon = "⚡",
        primary = Color(0xFF9333EA),
        primaryLight = Color(0xFFA855F7),
        primaryGlow = Color(0xFFC084FC),
        background = Color(0xFF090711),
        surface = Color(0xFF110D1F),
        surfaceCard = Color(0xFF181329),
        surfaceCardElevated = Color(0xFF241C3D),
        surfaceBorder = Color(0xFF322552),
        sentBubble = Color(0xFF7E22CE),
        receivedBubble = Color(0xFF1D172E),
        accent = Color(0xFF06B6D4)
    ),
    MATRIX_LIME(
        id = "matrix_lime",
        title = "Matrix Acid",
        subtitle = "Pitch black with radioactive neon green",
        icon = "🧪",
        primary = Color(0xFF10E576),
        primaryLight = Color(0xFF22C55E),
        primaryGlow = Color(0xFF4ADE80),
        background = Color(0xFF050806),
        surface = Color(0xFF0A110C),
        surfaceCard = Color(0xFF101A13),
        surfaceCardElevated = Color(0xFF18271D),
        surfaceBorder = Color(0xFF203828),
        sentBubble = Color(0xFF15803D),
        receivedBubble = Color(0xFF121C15),
        accent = Color(0xFFFACC15)
    ),
    TOKYO_SUNSET(
        id = "tokyo_sunset",
        title = "Tokyo Sunset",
        subtitle = "Neon magenta & coral cyberpunk warmth",
        icon = "🌅",
        primary = Color(0xFFFF2E93),
        primaryLight = Color(0xFFF43F5E),
        primaryGlow = Color(0xFFFB7185),
        background = Color(0xFF0E0611),
        surface = Color(0xFF170A1C),
        surfaceCard = Color(0xFF220F2A),
        surfaceCardElevated = Color(0xFF2F143B),
        surfaceBorder = Color(0xFF421B53),
        sentBubble = Color(0xFFE11D48),
        receivedBubble = Color(0xFF25112E),
        accent = Color(0xFFF97316)
    ),
    OCEAN_ABYSS(
        id = "ocean_abyss",
        title = "Abyss Aqua",
        subtitle = "Deep oceanic navy with glowing cyan & ice",
        icon = "🌊",
        primary = Color(0xFF06B6D4),
        primaryLight = Color(0xFF38BDF8),
        primaryGlow = Color(0xFF7DD3FC),
        background = Color(0xFF040914),
        surface = Color(0xFF071221),
        surfaceCard = Color(0xFF0B1B30),
        surfaceCardElevated = Color(0xFF102642),
        surfaceBorder = Color(0xFF17375E),
        sentBubble = Color(0xFF0284C7),
        receivedBubble = Color(0xFF0D1E33),
        accent = Color(0xFFA855F7)
    ),
    CRIMSON_VIPER(
        id = "crimson_viper",
        title = "Crimson Viper",
        subtitle = "Onyx dark mode with blood-red accents",
        icon = "🩸",
        primary = Color(0xFFEF4444),
        primaryLight = Color(0xFFF87171),
        primaryGlow = Color(0xFFFCA5A5),
        background = Color(0xFF080708),
        surface = Color(0xFF130C0E),
        surfaceCard = Color(0xFF1D1115),
        surfaceCardElevated = Color(0xFF2A171D),
        surfaceBorder = Color(0xFF3D1F27),
        sentBubble = Color(0xFFB91C1C),
        receivedBubble = Color(0xFF211317),
        accent = Color(0xFFF59E0B)
    )
}

val LocalAppTheme = compositionLocalOf { AppTheme.ELECTRIC_PURPLE }
