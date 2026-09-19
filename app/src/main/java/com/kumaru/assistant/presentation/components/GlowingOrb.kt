package com.kumaru.assistant.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.theme.AmberThinking
import com.kumaru.assistant.presentation.theme.CyberViolet
import com.kumaru.assistant.presentation.theme.ErrorCrimson
import com.kumaru.assistant.presentation.theme.NeonCyan

/**
 * Ultra-stable, high-performance canvas AI orb.
 *
 * Designed with fixed geometric layout constraints, hardware-accelerated draw passes,
 * and zero frame-to-frame layout jitter.
 *
 * States:
 * - IDLE: Gentle breathing pulse with cyan-violet celestial orbital node.
 * - LISTENING: Focused harmonic acoustic pulse rings.
 * - THINKING: Dual interlocking counter-rotating orbital rings with amber energy core.
 * - SPEAKING: Subtle rhythmic vocal aura waveforms.
 * - ERROR: Warning crimson aura pulse.
 */
@Composable
fun GlowingOrb(
    state: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfiniteTransition")

    // Smooth subtle breathing pulse (1.00f to 1.08f) - gentle, no violent scaling
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Smooth listening pulse
    val listeningPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ListeningPulse"
    )

    // Continuous smooth orbital rotation
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Reverse rotation for thinking state
    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbReverseRotation"
    )

    // Subtle wave progress
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbWaveProgress"
    )

    // Primary state color transition
    val primaryColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> NeonCyan
            AssistantState.LISTENING -> NeonCyan
            AssistantState.THINKING -> AmberThinking
            AssistantState.SPEAKING -> NeonCyan
            AssistantState.ERROR -> ErrorCrimson
        },
        animationSpec = tween(durationMillis = 350),
        label = "OrbPrimaryColor"
    )

    // Secondary state color transition
    val secondaryColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> CyberViolet
            AssistantState.LISTENING -> Color(0xFF00B4D8)
            AssistantState.THINKING -> CyberViolet
            AssistantState.SPEAKING -> CyberViolet
            AssistantState.ERROR -> AmberThinking
        },
        animationSpec = tween(durationMillis = 350),
        label = "OrbSecondaryColor"
    )

    // Outer container with strictly fixed dimensions - prevents any parent layout jitter
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = this.center
            val baseRadius = size.toPx() * 0.28f

            when (state) {
                AssistantState.IDLE -> {
                    drawIdleOrb(
                        center = center,
                        radius = baseRadius * pulseScale,
                        primary = primaryColor,
                        secondary = secondaryColor,
                        rotation = rotationDegrees
                    )
                }
                AssistantState.LISTENING -> {
                    drawListeningOrb(
                        center = center,
                        radius = baseRadius * listeningPulse,
                        primary = primaryColor,
                        secondary = secondaryColor,
                        waveProgress = waveProgress
                    )
                }
                AssistantState.THINKING -> {
                    drawThinkingOrb(
                        center = center,
                        radius = baseRadius,
                        primary = primaryColor,
                        secondary = secondaryColor,
                        forwardRotation = rotationDegrees * 2,
                        reverseRotation = reverseRotation
                    )
                }
                AssistantState.SPEAKING -> {
                    drawSpeakingOrb(
                        center = center,
                        radius = baseRadius * pulseScale,
                        primary = primaryColor,
                        secondary = secondaryColor,
                        waveProgress = waveProgress
                    )
                }
                AssistantState.ERROR -> {
                    drawErrorOrb(
                        center = center,
                        radius = baseRadius * pulseScale,
                        primary = primaryColor,
                        secondary = secondaryColor
                    )
                }
            }
        }
    }
}

// ---------------------- High Performance Canvas Draw Routines ----------------------

private fun DrawScope.drawIdleOrb(
    center: Offset,
    radius: Float,
    primary: Color,
    secondary: Color,
    rotation: Float
) {
    // 1. Soft atmospheric halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primary.copy(alpha = 0.20f),
                secondary.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.7f
        ),
        radius = radius * 1.7f,
        center = center
    )

    // 2. Stable orbital ring with rotating celestial node
    val ringRadius = radius * 1.35f
    drawCircle(
        color = primary.copy(alpha = 0.25f),
        radius = ringRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )

    rotate(rotation, center) {
        drawCircle(
            color = primary,
            radius = 3.5.dp.toPx(),
            center = Offset(center.x + ringRadius, center.y)
        )
    }

    // 3. Dense glowing inner core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                primary,
                secondary,
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawListeningOrb(
    center: Offset,
    radius: Float,
    primary: Color,
    secondary: Color,
    waveProgress: Float
) {
    // Stable harmonic acoustic pulse rings
    for (i in 0..2) {
        val currentProgress = (waveProgress + (i * 0.33f)) % 1f
        val ringRadius = radius * (1.05f + currentProgress * 0.55f)
        val alpha = (1f - currentProgress).coerceIn(0f, 1f) * 0.4f

        drawCircle(
            color = primary.copy(alpha = alpha),
            radius = ringRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // High energy core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                primary,
                secondary.copy(alpha = 0.5f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.15f
        ),
        radius = radius * 1.05f,
        center = center
    )
}

private fun DrawScope.drawThinkingOrb(
    center: Offset,
    radius: Float,
    primary: Color,
    secondary: Color,
    forwardRotation: Float,
    reverseRotation: Float
) {
    // Outer thinking aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                secondary.copy(alpha = 0.30f),
                primary.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.6f
        ),
        radius = radius * 1.6f,
        center = center
    )

    // Clockwise orbital arc
    val ringSize1 = Size(radius * 2.6f, radius * 2.6f)
    val ringTopLeft1 = Offset(center.x - radius * 1.3f, center.y - radius * 1.3f)
    rotate(forwardRotation, center) {
        drawArc(
            color = primary,
            startAngle = 0f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = ringTopLeft1,
            size = ringSize1,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = primary.copy(alpha = 0.35f),
            startAngle = 180f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = ringTopLeft1,
            size = ringSize1,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Counter-clockwise orbital arc
    val ringSize2 = Size(radius * 2.2f, radius * 2.2f)
    val ringTopLeft2 = Offset(center.x - radius * 1.1f, center.y - radius * 1.1f)
    rotate(reverseRotation, center) {
        drawArc(
            color = secondary,
            startAngle = 90f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = ringTopLeft2,
            size = ringSize2,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Core energy swirl
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                primary,
                secondary,
                Color.Transparent
            ),
            center = center,
            radius = radius * 0.9f
        ),
        radius = radius * 0.9f,
        center = center
    )
}

private fun DrawScope.drawSpeakingOrb(
    center: Offset,
    radius: Float,
    primary: Color,
    secondary: Color,
    waveProgress: Float
) {
    // Concentric harmonic speaking rings
    val ringCount = 2
    for (i in 0 until ringCount) {
        val currentProgress = (waveProgress + (i * 0.5f)) % 1f
        val currentRadius = radius * (1.1f + currentProgress * 0.45f)
        val alpha = (1f - currentProgress).coerceIn(0f, 1f) * 0.35f

        drawCircle(
            color = secondary.copy(alpha = alpha),
            radius = currentRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // Dynamic speaking halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primary.copy(alpha = 0.35f),
                secondary.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.5f
        ),
        radius = radius * 1.5f,
        center = center
    )

    // Core bright orb
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                primary,
                secondary,
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawErrorOrb(
    center: Offset,
    radius: Float,
    primary: Color,
    secondary: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primary.copy(alpha = 0.30f),
                secondary.copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.4f
        ),
        radius = radius * 1.4f,
        center = center
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.9f),
                primary,
                secondary,
                Color.Transparent
            ),
            center = center,
            radius = radius * 0.9f
        ),
        radius = radius * 0.9f,
        center = center
    )
}
