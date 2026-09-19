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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.theme.AmberThinking
import com.kumaru.assistant.presentation.theme.CyberViolet
import com.kumaru.assistant.presentation.theme.ErrorCrimson
import com.kumaru.assistant.presentation.theme.NeonCyan
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.VoidBlack

/**
 * Large, tactile push-to-talk button with dynamic ambient halo rings
 * and state-reactive icons.
 */
@Composable
fun TalkButton(
    state: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val infiniteTransition = rememberInfiniteTransition(label = "TalkHaloTransition")

    // Ambient glow pulse
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (state == AssistantState.LISTENING) 1.08f else 1f,
        animationSpec = tween(200),
        label = "ButtonScale"
    )

    val glowColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> NeonCyan
            AssistantState.LISTENING -> NeonCyan
            AssistantState.THINKING -> AmberThinking
            AssistantState.SPEAKING -> CyberViolet
            AssistantState.ERROR -> ErrorCrimson
        },
        label = "ButtonGlowColor"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(96.dp)
        ) {
            // Ambient outer glowing halo
            if (state == AssistantState.LISTENING || state == AssistantState.IDLE) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .scale(if (state == AssistantState.LISTENING) haloScale else 1.05f)
                        .clip(CircleShape)
                        .background(glowColor.copy(alpha = if (state == AssistantState.LISTENING) 0.28f else 0.10f))
                )
            }

            // Main tactile core button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.9f),
                                glowColor.copy(alpha = 0.6f),
                                VoidBlack
                            )
                        )
                    )
                    .border(1.5.dp, glowColor, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    AssistantState.IDLE -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Talk to Kumaru",
                            tint = VoidBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    AssistantState.LISTENING -> {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Listening",
                            tint = VoidBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    AssistantState.THINKING -> {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Thinking",
                            tint = VoidBlack,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    AssistantState.SPEAKING -> {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaking",
                            tint = VoidBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    AssistantState.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Retry",
                            tint = VoidBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = when (state) {
                AssistantState.IDLE -> "Tap to Talk"
                AssistantState.LISTENING -> "Listening… Tap to submit"
                AssistantState.THINKING -> "Kumaru is thinking…"
                AssistantState.SPEAKING -> "Tap to interrupt"
                AssistantState.ERROR -> "Tap to retry"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (state == AssistantState.LISTENING) NeonCyan else TextMuted
        )
    }
}
