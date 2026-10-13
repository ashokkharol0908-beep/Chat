package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleGlow
import com.example.ui.theme.ElectricPurpleLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SpinTheBottleDialog(
    onDismiss: () -> Unit,
    onConfirmSpin: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var hasSpun by remember { mutableStateOf(false) }

    fun doSpin() {
        if (isSpinning) return
        isSpinning = true
        coroutineScope.launch {
            val randomSpins = 3 + Random.nextInt(4)
            val randomExtraDegrees = Random.nextInt(360)
            val targetDegrees = rotationAnim.value + (randomSpins * 360f) + randomExtraDegrees

            rotationAnim.animateTo(
                targetValue = targetDegrees,
                animationSpec = tween(durationMillis = 2800, easing = FastOutSlowInEasing)
            )
            isSpinning = false
            hasSpun = true
            onConfirmSpin()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("spin_bottle_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SurfaceBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🍾", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Spin The Bottle",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "Randomly picks a homie for a challenge",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Arena Circle with Bottle
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF130E22))
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(ElectricPurple, NeonCyan, NeonPink, ElectricPurple)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(170.dp)) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // Draw concentric neon rings
                        drawCircle(
                            color = ElectricPurple.copy(alpha = 0.2f),
                            radius = size.width * 0.42f,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = NeonCyan.copy(alpha = 0.15f),
                            radius = size.width * 0.25f,
                            center = Offset(centerX, centerY)
                        )

                        // Draw rotating bottle
                        rotate(degrees = rotationAnim.value, pivot = Offset(centerX, centerY)) {
                            // Bottle Body (rounded rect)
                            val bottleW = 26.dp.toPx()
                            val bottleH = 92.dp.toPx()

                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(NeonCyan, ElectricPurpleGlow, ElectricPurple)
                                ),
                                topLeft = Offset(centerX - bottleW / 2f, centerY - bottleH / 2f + 14.dp.toPx()),
                                size = Size(bottleW, bottleH - 14.dp.toPx()),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )

                            // Bottle Neck
                            val neckW = 12.dp.toPx()
                            val neckH = 26.dp.toPx()
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(centerX - neckW / 2f, centerY - bottleH / 2f),
                                size = Size(neckW, neckH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )

                            // Bottle Cap (points in direction of pick)
                            drawCircle(
                                color = NeonPink,
                                radius = 6.dp.toPx(),
                                center = Offset(centerX, centerY - bottleH / 2f - 2.dp.toPx())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = if (isSpinning) "Spinning... who will fate choose? 👀"
                    else if (hasSpun) "Target locked! Challenge posted in chat! 🎯"
                    else "Tap Spin to randomly target a homie in this chat!",
                    color = if (hasSpun) NeonCyan else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { doSpin() },
                    enabled = !isSpinning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("spin_bottle_action_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricPurple
                    )
                ) {
                    Text(
                        text = if (isSpinning) "Spinning..." else if (hasSpun) "Spin Again 🍾" else "SPIN THE BOTTLE 🍾",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
