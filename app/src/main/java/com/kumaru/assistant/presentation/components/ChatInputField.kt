package com.kumaru.assistant.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.theme.AccentPinkGradientEnd
import com.kumaru.assistant.presentation.theme.AccentPinkGradientStart
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.GlassSurfaceWhite
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextOnPink
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary

/**
 * Floating glassmorphic interaction bar for Kumaru V0.2.2 directly inspired by the reference design.
 * Integrates "Ask me anything..." text field, dedicated Voice button, and Send action button.
 */
@Composable
fun ChatInputField(
    state: AssistantState,
    onSendMessage: (String) -> Unit,
    onVoiceClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isInputEnabled = state != AssistantState.THINKING && state != AssistantState.SPEAKING
    val hasText = text.trim().isNotEmpty()
    val canSend = isInputEnabled && hasText

    val handleSend = {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty() && isInputEnabled) {
            onSendMessage(trimmed)
            text = ""
            keyboardController?.hide()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color(0x18000000),
                spotColor = Color(0x22E05697)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(GlassSurfaceWhite)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        GlassBorderLight,
                        Color(0x40CBD5E1)
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left sparkle / assist icon button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x0C1E1B2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "AI Sparkle",
                    tint = AccentPinkPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text input container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = when (state) {
                            AssistantState.THINKING -> "Kumaru is thinking…"
                            AssistantState.SPEAKING -> "Kumaru is speaking…"
                            AssistantState.LISTENING -> "Listening to you…"
                            else -> "Ask me anything..."
                        },
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = { if (isInputEnabled) text = it },
                    enabled = isInputEnabled,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(AccentPinkPrimary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { handleSend() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons Group: Voice & Send
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Dedicated Voice Capsule Button
                val isListening = state == AssistantState.LISTENING
                val voiceBgColor by animateColorAsState(
                    targetValue = if (isListening) AccentPinkPrimary else Color(0x0C1E1B2E),
                    label = "VoiceBgColor"
                )
                val voiceContentColor by animateColorAsState(
                    targetValue = if (isListening) TextOnPink else TextSecondary,
                    label = "VoiceContentColor"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(voiceBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = isInputEnabled,
                            onClick = onVoiceClick
                        )
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Voice Input",
                            tint = voiceContentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isListening) "Stop" else "Voice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = voiceContentColor
                        )
                    }
                }

                // Send Capsule Button (Vibrant Pink Gradient from reference)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (canSend) {
                                Brush.horizontalGradient(
                                    listOf(AccentPinkGradientStart, AccentPinkGradientEnd)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0x18E05697), Color(0x18D93B84))
                                )
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = canSend,
                            onClick = { handleSend() }
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = if (canSend) TextOnPink else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Send",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (canSend) TextOnPink else TextMuted
                        )
                    }
                }
            }
        }
    }
}
