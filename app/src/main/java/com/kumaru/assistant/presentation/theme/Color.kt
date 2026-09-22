package com.kumaru.assistant.presentation.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// KUMARU V0.2.2 LIGHT GLASSMORPHIC PALETTE
// Based directly on the reference design
// ==========================================

// Background Atmospheric Canvas
val BackgroundWarmBase = Color(0xFFFAF8FC)
val BackgroundGradientTop = Color(0xFFFCFAFD)
val BackgroundGradientBottom = Color(0xFFF4EEF7)
val BackgroundAtmospherePink = Color(0x38F472B6)
val BackgroundAtmosphereLilac = Color(0x28C084FC)
val BackgroundAtmosphereCyan = Color(0x2038BDF8)
val BackgroundGridLine = Color(0x0C1E1B2E) // ~5% opacity fine geometric grid

// Glassmorphism Materials
val GlassSurfaceWhite = Color(0xE6FFFFFF)       // 90% white frosted
val GlassSurfaceTranslucent = Color(0xB8FFFFFF) // 72% white frosted
val GlassSurfaceSubtle = Color(0x80FFFFFF)      // 50% white frosted
val GlassSurfacePinkTint = Color(0x26F472B6)    // 15% pink tint
val GlassSurfaceCyanTint = Color(0x1A38BDF8)    // 10% cyan tint

val GlassBorderLight = Color(0xB3FFFFFF)        // 70% white edge
val GlassBorderSubtle = Color(0x40CBD5E1)       // Soft gray boundary
val GlassBorderPink = Color(0x66F472B6)         // Soft pink edge
val GlassBorderCyan = Color(0x4D38BDF8)         // Soft cyan edge

// Primary Brand & Action Accents (Vibrant Magenta / Soft Rose Pink)
val AccentPinkPrimary = Color(0xFFE05697)
val AccentPinkSecondary = Color(0xFFD63384)
val AccentPinkGradientStart = Color(0xFFE860A3)
val AccentPinkGradientEnd = Color(0xFFD93B84)
val AccentPinkSubtleBg = Color(0xFFFDF2F7)
val AccentPinkBorder = Color(0x4DE05697)

// AI Glossy 3D Orb Treatment Colors
val OrbPinkTop = Color(0xFFFF5B99)
val OrbPinkMid = Color(0xFFF472B6)
val OrbLilac = Color(0xFFC084FC)
val OrbLavenderLight = Color(0xFFEDE9FE)
val OrbCyanBottom = Color(0xFF38BDF8)
val OrbTurquoise = Color(0xFF2DD4BF)
val OrbTealGlow = Color(0xFF06B6D4)
val OrbSpecularPure = Color(0xF2FFFFFF)
val OrbSpecularSoft = Color(0x99FFFFFF)
val OrbReflectionPink = Color(0x3DF472B6)
val OrbReflectionCyan = Color(0x3338BDF8)

// State-Specific Status Palette
val StateIdle = Color(0xFF10B981)          // Emerald Ready
val StateListening = Color(0xFFE05697)     // Active Pink Mic
val StateThinking = Color(0xFFF59E0B)      // Warm Amber Reasoning
val StateSpeaking = Color(0xFF8B5CF6)      // Violet Synthesis
val StateError = Color(0xFFEF4444)         // Crimson Alert

val StateIdleBg = Color(0x1A10B981)
val StateListeningBg = Color(0x24E05697)
val StateThinkingBg = Color(0x24F59E0B)
val StateSpeakingBg = Color(0x248B5CF6)
val StateErrorBg = Color(0x24EF4444)

// High-Legibility Modern Typography Tokens (Dark on Light Glass)
val TextPrimary = Color(0xFF181824)        // Confident Dark Grotesk
val TextSecondary = Color(0xFF5B6477)      // Soft Neutral Slate
val TextTertiary = Color(0xFF8E99AC)       // Refined Subtle Gray
val TextMuted = Color(0xFFA5B0C2)          // Hint / Placeholder
val TextOnPink = Color(0xFFFFFFFF)         // White on Pink Buttons / Bubbles
val TextAccentPink = Color(0xFFDB2777)     // Pink Highlighted Typography

// Legacy compatibility aliases if needed
val VoidBlack = BackgroundWarmBase
val DarkObsidian = BackgroundWarmBase
val DeepSurface = GlassSurfaceWhite
val GlassSurface = GlassSurfaceTranslucent
val GlassBorder = GlassBorderLight
val NeonCyan = OrbCyanBottom
val CyberViolet = OrbLilac
val AmberThinking = StateThinking
val ErrorCrimson = StateError
val SuccessEmerald = StateIdle
