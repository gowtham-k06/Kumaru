package com.kumaru.assistant.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kumaru.assistant.presentation.theme.BackgroundAtmosphereCyan
import com.kumaru.assistant.presentation.theme.BackgroundAtmosphereLilac
import com.kumaru.assistant.presentation.theme.BackgroundAtmospherePink
import com.kumaru.assistant.presentation.theme.BackgroundGradientBottom
import com.kumaru.assistant.presentation.theme.BackgroundGradientTop
import com.kumaru.assistant.presentation.theme.BackgroundGridLine
import com.kumaru.assistant.presentation.theme.BackgroundWarmBase

/**
 * Reusable editorial background for Kumaru V0.2.2.
 *
 * Layers:
 * 1. Warm off-white linear gradient base.
 * 2. Subtle soft pink & lilac atmospheric radial glows.
 * 3. Soft cyan illumination near orb area.
 * 4. Deterministic, sub-pixel fine geometric grid pattern for the tactile editorial look.
 */
@Composable
fun KumaruBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundGradientTop,
                        BackgroundWarmBase,
                        BackgroundGradientBottom
                    )
                )
            )
    ) {
        // Atmospheric glow and fine geometric grid canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Top-Right Soft Pink Atmospheric Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BackgroundAtmospherePink,
                        BackgroundAtmosphereLilac.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.15f),
                    radius = width * 0.9f
                ),
                center = Offset(width * 0.85f, height * 0.15f),
                radius = width * 0.9f
            )

            // 2. Center / Orb Area Soft Cyan & Lilac Atmosphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BackgroundAtmosphereCyan,
                        BackgroundAtmospherePink.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.38f),
                    radius = width * 0.75f
                ),
                center = Offset(width * 0.5f, height * 0.38f),
                radius = width * 0.75f
            )

            // 3. Bottom Soft Pink Atmosphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BackgroundAtmospherePink.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.2f, height * 0.92f),
                    radius = width * 0.7f
                ),
                center = Offset(width * 0.2f, height * 0.92f),
                radius = width * 0.7f
            )

            // 4. Fine Geometric Grid Lines (Crisp, deterministic, ultra-subtle)
            val gridSizePx = 28.dp.toPx()
            val strokeWidthPx = 0.75.dp.toPx()

            // Vertical grid lines
            var x = 0f
            while (x <= width) {
                drawLine(
                    color = BackgroundGridLine,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = strokeWidthPx
                )
                x += gridSizePx
            }

            // Horizontal grid lines
            var y = 0f
            while (y <= height) {
                drawLine(
                    color = BackgroundGridLine,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = strokeWidthPx
                )
                y += gridSizePx
            }
        }

        // Foreground application content
        content()
    }
}
