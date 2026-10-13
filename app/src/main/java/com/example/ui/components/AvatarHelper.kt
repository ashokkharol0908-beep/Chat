package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleGlow
import com.example.ui.theme.ElectricPurpleLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun HomieAvatar(
    avatarKey: String,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    moodEmoji: String? = null,
    hasStoryRing: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val gradient = remember(avatarKey) {
        when {
            avatarKey.contains("1") || avatarKey.contains("alex") -> Brush.linearGradient(
                listOf(ElectricPurple, NeonPink)
            )
            avatarKey.contains("2") || avatarKey.contains("maya") -> Brush.linearGradient(
                listOf(NeonCyan, Color(0xFF6366F1))
            )
            avatarKey.contains("3") || avatarKey.contains("leo") -> Brush.linearGradient(
                listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
            )
            avatarKey.contains("4") || avatarKey.contains("chloe") -> Brush.linearGradient(
                listOf(NeonPink, Color(0xFFA855F7))
            )
            avatarKey.contains("5") || avatarKey.contains("sam") -> Brush.linearGradient(
                listOf(Color(0xFF10B981), Color(0xFF06B6D4))
            )
            avatarKey.contains("group") -> Brush.linearGradient(
                listOf(ElectricPurpleGlow, NeonCyan)
            )
            avatarKey.contains("chaos") -> Brush.linearGradient(
                listOf(Color(0xFFFF007A), Color(0xFF7928CA))
            )
            avatarKey.contains("bottle") -> Brush.linearGradient(
                listOf(Color(0xFF00DFD8), Color(0xFF007CF0))
            )
            avatarKey.contains("anon") -> Brush.linearGradient(
                listOf(Color(0xFF475569), Color(0xFF1E293B))
            )
            else -> Brush.linearGradient(
                listOf(ElectricPurpleLight, Color(0xFF3B82F6))
            )
        }
    }

    val initials = remember(displayName) {
        when {
            displayName.contains("Anonymous") -> "🎭"
            displayName.contains("CHAOS") -> "😈"
            displayName.contains("Spin") -> "🍾"
            displayName.contains("Inner Circle") -> "⚡"
            displayName.contains("Arcade") -> "🎮"
            displayName.isNotBlank() -> {
                val parts = displayName.trim().split(" ")
                if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                else displayName.take(2).uppercase()
            }
            else -> "H"
        }
    }

    Box(
        modifier = modifier
            .size(if (hasStoryRing) size + 6.dp else size)
            .then(
                if (hasStoryRing) {
                    Modifier
                        .border(
                            width = 2.5.dp,
                            brush = Brush.sweepGradient(listOf(ElectricPurpleLight, NeonPink, NeonCyan, ElectricPurpleLight)),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                } else Modifier
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Online dot indicator
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(NeonGreen)
                    .border(2.dp, DeepBlack, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        // Mood emoji badge
        if (moodEmoji != null && !isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.36f)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1A30))
                    .border(1.dp, Color(0xFF3F355C), CircleShape)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = moodEmoji,
                    fontSize = (size.value * 0.22f).sp
                )
            }
        }
    }
}

@Composable
fun AudioWaveformPlayer(
    durationSec: Int,
    waveformData: String?,
    isSentByUser: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }

    val rawBars = remember(waveformData) {
        if (!waveformData.isNullOrBlank()) {
            waveformData.split(",").mapNotNull { it.trim().toFloatOrNull() }
        } else {
            listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.7f, 0.4f, 0.8f, 0.5f, 0.3f, 0.7f, 0.4f)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val stepTimeMs = 50L
            val totalSteps = (durationSec * 1000) / stepTimeMs
            for (step in 0..totalSteps) {
                if (!isPlaying) break
                currentProgress = step.toFloat() / totalSteps.toFloat()
                delay(stepTimeMs)
            }
            isPlaying = false
            currentProgress = 0f
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                isPlaying = !isPlaying
                if (isPlaying && currentProgress >= 1f) currentProgress = 0f
            },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSentByUser) Color.White.copy(alpha = 0.25f) else ElectricPurple.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Waveform bars
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .padding(horizontal = 8.dp)
        ) {
            val barWidth = 3.dp.toPx()
            val spacing = 2.5.dp.toPx()
            val totalBarWidth = barWidth + spacing
            val barCount = rawBars.size.coerceAtLeast(1)
            val availableWidth = size.width
            val step = (availableWidth / barCount).coerceAtLeast(totalBarWidth)

            rawBars.forEachIndexed { index, heightRatio ->
                val x = index * step
                val barHeight = (size.height * heightRatio.coerceIn(0.15f, 0.95f))
                val y = (size.height - barHeight) / 2f
                val barProgress = index.toFloat() / barCount.toFloat()
                val barColor = if (barProgress <= currentProgress) {
                    if (isSentByUser) Color.White else NeonCyan
                } else {
                    if (isSentByUser) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.25f)
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }

        Text(
            text = "${durationSec}s",
            color = if (isSentByUser) Color.White.copy(alpha = 0.85f) else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
