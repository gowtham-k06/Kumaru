package com.kumaru.assistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kumaru.assistant.presentation.theme.CyberViolet
import com.kumaru.assistant.presentation.theme.DarkObsidian
import com.kumaru.assistant.presentation.theme.NeonCyan
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextPrimary

/**
 * Futuristic, glassmorphic text input field for Kumaru V0.2.1.
 * Supports keyboard enter/send, non-empty validation, and state-reactive controls.
 */
@Composable
fun ChatInputField(
    state: AssistantState,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isInputEnabled = state != AssistantState.THINKING && state != AssistantState.SPEAKING
    val canSend = isInputEnabled && text.trim().isNotEmpty()

    val handleSend = {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty() && isInputEnabled) {
            onSendMessage(trimmed)
            text = ""
            keyboardController?.hide()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x1AFFFFFF))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = if (canSend) {
                        listOf(NeonCyan.copy(alpha = 0.6f), CyberViolet.copy(alpha = 0.6f))
                    } else {
                        listOf(Color(0x2200E5FF), Color(0x117C4DFF))
                    }
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isEmpty()) {
                Text(
                    text = when (state) {
                        AssistantState.THINKING -> "Kumaru is thinking…"
                        AssistantState.SPEAKING -> "Kumaru is speaking…"
                        AssistantState.LISTENING -> "Or type your message…"
                        else -> "Ask Kumaru anything…"
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
                cursorBrush = SolidColor(NeonCyan),
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

        IconButton(
            onClick = { handleSend() },
            enabled = canSend,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (canSend) {
                        Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.3f), CyberViolet.copy(alpha = 0.3f)))
                    } else {
                        SolidColor(Color.Transparent)
                    }
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
                tint = if (canSend) NeonCyan else TextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
