package com.kumaru.assistant.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AccentPinkPrimary,
    onPrimary = TextOnPink,
    primaryContainer = AccentPinkSubtleBg,
    onPrimaryContainer = TextAccentPink,
    secondary = OrbLilac,
    onSecondary = TextOnPink,
    background = BackgroundWarmBase,
    onBackground = TextPrimary,
    surface = GlassSurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceTranslucent,
    onSurfaceVariant = TextSecondary,
    error = StateError,
    onError = TextOnPink
)

@Composable
fun KumaruTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Ensure edge-to-edge with light status bars and navigation bars
                window.statusBarColor = BackgroundWarmBase.toArgb()
                window.navigationBarColor = BackgroundWarmBase.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = KumaruTypography,
        content = content
    )
}
