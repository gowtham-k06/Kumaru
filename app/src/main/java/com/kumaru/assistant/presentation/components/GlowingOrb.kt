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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, multi-layered canvas AI orb.
 *
 * Visually reacts to the 5 Kumaru states:
 * - IDLE: Gentle breathing pulse with cyan & violet orbital glow.
 * - LISTENING: High-frequency acoustic ripple waves.
 * - THINKING: Dual counter-rotating orbital rings with amber-violet energy core.
 * - SPEAKING: Harmonically modulated concentric vocal waveform ripples.
 * - ERROR: Warning amber-crimson aura pulse.
 */
@Composable
fun GlowingOrb(
    state: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbAnimation")

    // Breathing pulse (used across idle/speaking)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Rapid pulse for listening / thinking
    val rapidPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RapidPulse"
    )

    // Continuous orbital rotation
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // Reverse fast rotation for thinking state
    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReverseRotation"
    )

    // Wave ripple phase for speaking / listening
    val ripplePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RipplePhase"
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
        animationSpec = tween(durationMillis = 400),
        label = "PrimaryColor"
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
        animationSpec = tween(durationMillis = 400),
        label = "SecondaryColor"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = this.center
            val baseRadius = size.toPx() / 3.4f

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
                        radius = baseRadius * rapidPulse,
                        primary = primaryColor,
                        secondary = secondaryColor,
                        ripplePhase = ripplePhase
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
                        ripplePhase = ripplePhase
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

// ---------------------- Canvas Draw Routines ----------------------

private fun DrawScope.drawIdleOrb(
    center: Offset,
    radius: Float,
    primary: Color,
    secondary: Color,
    rotation: Float
) {
    // Outer atmospheric halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primary.copy(alpha = 0.25f),
                secondary.copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.8f
        ),
        radius = radius * 1.8f,
        center = center
    )

    // Orbital ring with rotating node
    rotate(rotation, center) {
        drawCircle(
            color = primary.copy(alpha = 0.35f),
            radius = radius * 1.35f,
            center = center,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        val nodeX = center.x + radius * 1.35f
        val nodeY = center.y
        drawCircle(
            color = primary,
            radius = 4.dp.toPx(),
            center = Offset(nodeX, nodeY)
        )
    }

    // Dense inner core
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
    ripplePhase: Float
) {
    // Expanding acoustic shockwaves
    for (i in 0..2) {
        val waveProgress = (ripplePhase + (i * 0.33f)) % 1f
        val waveRadius = radius * (1f + waveProgress * 0.85f)
        val alpha = (1f - waveProgress).coerceIn(0f, 1f) * 0.5f

        drawCircle(
            color = primary.copy(alpha = alpha),
            radius = waveRadius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
    }

    // High energy core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                primary,
                secondary.copy(alpha = 0.6f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.2f
        ),
        radius = radius * 1.1f,
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
                secondary.copy(alpha = 0.35f),
                primary.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.7f
        ),
        radius = radius * 1.7f,
        center = center
    )

    // Interlocking orbital ring 1 (clockwise)
    rotate(forwardRotation, center) {
        drawArc(
            color = primary,
            startAngle = 30f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 1.35f, center.y - radius * 1.35f),
            size = Size(radius * 2.7f, radius * 2.7f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = primary.copy(alpha = 0.4f),
            startAngle = 210f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 1.35f, center.y - radius * 1.35f),
            size = Size(radius * 2.7f, radius * 2.7f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Interlocking orbital ring 2 (counter-clockwise)
    rotate(reverseRotation, center) {
        drawArc(
            color = secondary,
            startAngle = 90f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 1.15f, center.y - radius * 1.15f),
            size = Size(radius * 2.3f, radius * 2.3f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Core swirl
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

private fun DrawScope.drawSpeakingOrb(
    center: Offset,
    radius: Float,
    primary: Color,
    secondary: Color,
    ripplePhase: Float
) {
    // Vocal concentric harmonic rings
    val waveSteps = 12
    val waveRadius = radius * 1.4f
    for (i in 0 until waveSteps) {
        val angle = (i * (360f / waveSteps)) * (PI / 180f).toFloat()
        val modulation = sin((ripplePhase * 2 * PI + i).toFloat()) * 12.dp.toPx()
        val currentR = waveRadius + modulation
        val px = center.x + currentR * cos(angle)
        val py = center.y + currentR * sin(angle)

        drawCircle(
            color = primary.copy(alpha = 0.5f),
            radius = 3.dp.toPx(),
            center = Offset(px, py)
        )
    }

    // Dynamic pulsating halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primary.copy(alpha = 0.4f),
                secondary.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.6f
        ),
        radius = radius * 1.6f,
        center = center
    )

    // Inner bright core
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
