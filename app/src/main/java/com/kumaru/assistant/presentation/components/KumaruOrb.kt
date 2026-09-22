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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.theme.OrbCyanBottom
import com.kumaru.assistant.presentation.theme.OrbLilac
import com.kumaru.assistant.presentation.theme.OrbPinkMid
import com.kumaru.assistant.presentation.theme.OrbPinkTop
import com.kumaru.assistant.presentation.theme.OrbReflectionCyan
import com.kumaru.assistant.presentation.theme.OrbReflectionPink
import com.kumaru.assistant.presentation.theme.OrbSpecularPure
import com.kumaru.assistant.presentation.theme.OrbSpecularSoft
import com.kumaru.assistant.presentation.theme.OrbTealGlow
import com.kumaru.assistant.presentation.theme.OrbTurquoise
import com.kumaru.assistant.presentation.theme.StateError
import com.kumaru.assistant.presentation.theme.StateThinking
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium 3D Glossy Liquid Orb for Kumaru V0.2.2.
 * Directly recreates the physical glass sphere, multi-tone magenta/cyan refractions,
 * top-left specular highlights, and soft floor reflection from the visual reference.
 */
@Composable
fun KumaruOrb(
    state: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "KumaruOrbTransition")

    // Ultra-smooth breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingPulse"
    )

    // Gentle vertical float offset (in pixels)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )

    // Listening acoustic wave progression
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveProgress"
    )

    // Thinking rotation angle
    val thinkingAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ThinkingAngle"
    )

    // Dynamic top color transition
    val topOrbColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> OrbPinkTop
            AssistantState.LISTENING -> OrbPinkTop
            AssistantState.THINKING -> StateThinking
            AssistantState.SPEAKING -> OrbLilac
            AssistantState.ERROR -> StateError
        },
        animationSpec = tween(400),
        label = "TopOrbColor"
    )

    // Dynamic bottom color transition
    val bottomOrbColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> OrbCyanBottom
            AssistantState.LISTENING -> OrbTealGlow
            AssistantState.THINKING -> OrbPinkMid
            AssistantState.SPEAKING -> OrbTurquoise
            AssistantState.ERROR -> StateThinking
        },
        animationSpec = tween(400),
        label = "BottomOrbColor"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val sphereRadius = (canvasWidth * 0.36f) * if (state == AssistantState.LISTENING) (breathingPulse * 1.02f) else breathingPulse
            val orbCenter = Offset(canvasWidth * 0.5f, (canvasHeight * 0.44f) + floatOffset)

            // 1. Soft Floor Shadow & Diffused Reflection (matching reference reflection)
            drawOrbFloorReflection(
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                sphereRadius = sphereRadius,
                topColor = topOrbColor,
                bottomColor = bottomOrbColor
            )

            // 2. Soft Ambient Halo Glow behind sphere
            drawOrbAmbientHalo(
                center = orbCenter,
                radius = sphereRadius,
                topColor = topOrbColor,
                bottomColor = bottomOrbColor,
                state = state,
                waveProgress = waveProgress
            )

            // 3. Acoustic Pulse Rings (when LISTENING or SPEAKING)
            if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
                drawAcousticRings(
                    center = orbCenter,
                    radius = sphereRadius,
                    waveProgress = waveProgress,
                    color = if (state == AssistantState.LISTENING) OrbPinkMid else OrbCyanBottom
                )
            }

            // 4. Main 3D Spherical Liquid Body
            drawMainGlossySphere(
                center = orbCenter,
                radius = sphereRadius,
                topColor = topOrbColor,
                bottomColor = bottomOrbColor,
                state = state,
                thinkingAngle = thinkingAngle
            )

            // 5. Inner Glass Refraction & Bottom Cyan Caustics
            drawInnerCaustics(
                center = orbCenter,
                radius = sphereRadius,
                bottomColor = bottomOrbColor
            )

            // 6. 3D Glossy Specular Highlights (crescent curvature)
            drawSpecularHighlights(
                center = orbCenter,
                radius = sphereRadius
            )

            // 7. Ultra-fine outer glass refraction rim
            drawCircle(
                color = Color.White.copy(alpha = 0.55f),
                radius = sphereRadius,
                center = orbCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

/**
 * Mini avatar version of the 3D glossy orb used beside Kumaru responses in the transcript.
 */
@Composable
fun KumaruMiniOrb(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.width * 0.45f
        val center = Offset(this.size.width * 0.5f, this.size.height * 0.5f)

        // Main sphere
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    OrbPinkTop,
                    OrbLilac,
                    OrbCyanBottom
                ),
                startY = center.y - radius,
                endY = center.y + radius
            ),
            radius = radius,
            center = center
        )

        // Inner refraction
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.55f),
                    Color.Transparent
                ),
                center = Offset(center.x, center.y - radius * 0.3f),
                radius = radius * 0.7f
            ),
            radius = radius * 0.7f,
            center = Offset(center.x, center.y - radius * 0.3f)
        )

        // Specular dot
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = radius * 0.28f,
            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
        )

        // Glass edge
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = radius,
            center = center,
            style = Stroke(width = 0.75.dp.toPx())
        )
    }
}

// ---------------------- Canvas Draw Implementations ----------------------

private fun DrawScope.drawOrbFloorReflection(
    canvasWidth: Float,
    canvasHeight: Float,
    sphereRadius: Float,
    topColor: Color,
    bottomColor: Color
) {
    val floorCenter = Offset(canvasWidth * 0.5f, canvasHeight * 0.88f)
    val shadowWidth = sphereRadius * 1.8f
    val shadowHeight = sphereRadius * 0.35f

    // Soft diffused floor shadow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x18000000),
                Color(0x0A000000),
                Color.Transparent
            ),
            center = floorCenter,
            radius = shadowWidth * 0.6f
        ),
        topLeft = Offset(floorCenter.x - shadowWidth * 0.5f, floorCenter.y - shadowHeight * 0.5f),
        size = Size(shadowWidth, shadowHeight)
    )

    // Soft colored specular reflection pool (pink & cyan glow on the ground)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                OrbReflectionPink,
                OrbReflectionCyan,
                Color.Transparent
            ),
            center = Offset(floorCenter.x, floorCenter.y - 2.dp.toPx()),
            radius = shadowWidth * 0.5f
        ),
        topLeft = Offset(floorCenter.x - shadowWidth * 0.4f, floorCenter.y - shadowHeight * 0.4f),
        size = Size(shadowWidth * 0.8f, shadowHeight * 0.8f)
    )
}

private fun DrawScope.drawOrbAmbientHalo(
    center: Offset,
    radius: Float,
    topColor: Color,
    bottomColor: Color,
    state: AssistantState,
    waveProgress: Float
) {
    val haloRadius = radius * 1.55f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                topColor.copy(alpha = 0.22f),
                bottomColor.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = center,
            radius = haloRadius
        ),
        radius = haloRadius,
        center = center
    )
}

private fun DrawScope.drawAcousticRings(
    center: Offset,
    radius: Float,
    waveProgress: Float,
    color: Color
) {
    for (i in 0..2) {
        val currentProgress = (waveProgress + (i * 0.33f)) % 1f
        val ringRadius = radius * (1.06f + currentProgress * 0.42f)
        val alpha = (1f - currentProgress).coerceIn(0f, 1f) * 0.35f

        drawCircle(
            color = color.copy(alpha = alpha),
            radius = ringRadius,
            center = center,
            style = Stroke(width = 1.25.dp.toPx())
        )
    }
}

private fun DrawScope.drawMainGlossySphere(
    center: Offset,
    radius: Float,
    topColor: Color,
    bottomColor: Color,
    state: AssistantState,
    thinkingAngle: Float
) {
    if (state == AssistantState.THINKING) {
        rotate(thinkingAngle, center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        topColor,
                        OrbLilac,
                        bottomColor,
                        OrbTurquoise,
                        topColor
                    ),
                    center = center
                ),
                radius = radius,
                center = center
            )
        }
    } else {
        // Multi-stop 3D liquid vertical gradient
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    topColor,
                    OrbPinkMid,
                    OrbLilac,
                    OrbCyanBottom,
                    bottomColor
                ),
                startY = center.y - radius,
                endY = center.y + radius
            ),
            radius = radius,
            center = center
        )
    }

    // Secondary 3D volume gradient (gives sphere spherical depth)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x22000000)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawInnerCaustics(
    center: Offset,
    radius: Float,
    bottomColor: Color
) {
    // Bottom inner cyan glow reflection / caustic light
    val bottomCausticCenter = Offset(center.x, center.y + radius * 0.5f)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                bottomColor.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.35f),
                Color.Transparent
            ),
            center = bottomCausticCenter,
            radius = radius * 0.55f
        ),
        topLeft = Offset(center.x - radius * 0.6f, center.y + radius * 0.2f),
        size = Size(radius * 1.2f, radius * 0.7f)
    )

    // Center internal luminous core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = Offset(center.x, center.y - radius * 0.1f),
            radius = radius * 0.5f
        ),
        radius = radius * 0.5f,
        center = Offset(center.x, center.y - radius * 0.1f)
    )
}

private fun DrawScope.drawSpecularHighlights(
    center: Offset,
    radius: Float
) {
    // Primary glossy specular highlight: top-left curved oval / crescent
    val highlightCenter = Offset(center.x - radius * 0.32f, center.y - radius * 0.36f)
    val highlightWidth = radius * 0.72f
    val highlightHeight = radius * 0.38f

    rotate(degrees = -28f, pivot = highlightCenter) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    OrbSpecularPure,
                    OrbSpecularSoft,
                    Color.Transparent
                ),
                center = highlightCenter,
                radius = highlightWidth * 0.5f
            ),
            topLeft = Offset(highlightCenter.x - highlightWidth * 0.5f, highlightCenter.y - highlightHeight * 0.5f),
            size = Size(highlightWidth, highlightHeight)
        )
    }

    // Secondary subtle glossy highlight near top-right edge
    val secondaryHighlightCenter = Offset(center.x + radius * 0.45f, center.y - radius * 0.32f)
    drawCircle(
        color = Color.White.copy(alpha = 0.45f),
        radius = radius * 0.12f,
        center = secondaryHighlightCenter
    )
}
