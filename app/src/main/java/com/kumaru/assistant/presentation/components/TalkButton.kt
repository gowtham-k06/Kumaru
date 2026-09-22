package com.kumaru.assistant.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.theme.AccentPinkGradientEnd
import com.kumaru.assistant.presentation.theme.AccentPinkGradientStart
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.StateError
import com.kumaru.assistant.presentation.theme.StateIdle
import com.kumaru.assistant.presentation.theme.StateListening
import com.kumaru.assistant.presentation.theme.StateSpeaking
import com.kumaru.assistant.presentation.theme.StateThinking
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextOnPink
import com.kumaru.assistant.presentation.theme.TextSecondary

/**
 * Tactile glassmorphic voice button for Kumaru V0.2.2.
 */
@Composable
fun TalkButton(
    state: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val infiniteTransition = rememberInfiniteTransition(label = "TalkHaloTransition")

    val isInteractive = state == AssistantState.IDLE || state == AssistantState.LISTENING || state == AssistantState.ERROR

    // Ambient glow pulse
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (state == AssistantState.LISTENING) 1.06f else 1f,
        animationSpec = tween(200),
        label = "ButtonScale"
    )

    val glowColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> AccentPinkPrimary
            AssistantState.LISTENING -> StateListening
            AssistantState.THINKING -> StateThinking
            AssistantState.SPEAKING -> StateSpeaking
            AssistantState.ERROR -> StateError
        },
        label = "ButtonGlowColor"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            // Ambient outer glowing halo
            if (state == AssistantState.LISTENING || state == AssistantState.IDLE) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(if (state == AssistantState.LISTENING) haloScale else 1.04f)
                        .clip(CircleShape)
                        .background(glowColor.copy(alpha = if (state == AssistantState.LISTENING) 0.22f else 0.08f))
                )
            }

            // Main tactile core button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(buttonScale)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        ambientColor = Color(0x18000000),
                        spotColor = Color(0x33E05697)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AccentPinkGradientStart,
                                AccentPinkGradientEnd
                            )
                        )
                    )
                    .border(
                        1.25.dp,
                        GlassBorderLight,
                        CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = isInteractive,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    AssistantState.IDLE -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Talk to Kumaru",
                            tint = TextOnPink,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    AssistantState.LISTENING -> {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Listening",
                            tint = TextOnPink,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    AssistantState.THINKING -> {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Thinking",
                            tint = TextOnPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    AssistantState.SPEAKING -> {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaking",
                            tint = TextOnPink,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    AssistantState.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Retry",
                            tint = TextOnPink,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = when (state) {
                AssistantState.IDLE -> "Tap to Talk"
                AssistantState.LISTENING -> "Listening… Tap to submit"
                AssistantState.THINKING -> "Kumaru is thinking…"
                AssistantState.SPEAKING -> "Kumaru is speaking…"
                AssistantState.ERROR -> "Tap to return to ready"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (state == AssistantState.LISTENING) AccentPinkPrimary else TextSecondary
        )
    }
}
