package com.kumaru.assistant.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.MessageRole
import com.kumaru.assistant.presentation.theme.CyberViolet
import com.kumaru.assistant.presentation.theme.NeonCyan
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary

/**
 * Non-boxy, cinematic conversation transcript stream.
 * Prioritizes high legibility and fluid modern aesthetics over heavy cards.
 */
@Composable
fun TranscriptView(
    messages: List<ConversationMessage>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty()) {
        EmptyTranscriptSuggestions(
            onSuggestionClick = onSuggestionClick,
            modifier = modifier
        )
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                TranscriptMessageItem(message = message)
            }
        }
    }
}

@Composable
private fun TranscriptMessageItem(message: ConversationMessage) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Kumaru glowing dot indicator
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(
                            listOf(
                                Color(0x2E00E5FF),
                                Color(0x1A0077B6)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0x1F1F2438),
                                Color(0x14101424)
                            )
                        )
                    }
                )
                .border(
                    width = 0.5.dp,
                    brush = if (isUser) {
                        Brush.linearGradient(listOf(Color(0x6600E5FF), Color(0x2200E5FF)))
                    } else {
                        Brush.linearGradient(listOf(Color(0x337C4DFF), Color(0x1100E5FF)))
                    },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "YOU" else "KUMARU",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isUser) NeonCyan else CyberViolet
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyTranscriptSuggestions(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "Enna Kumaru, epdi irukinga?",
        "Who are you and what can you do?",
        "Tell me something interesting about space",
        "What are you building in stage V0.2?"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "KUMARU V0.2 VOICE ACTIVE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tap the microphone below or choose a query:",
            fontSize = 13.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(14.dp))

        suggestions.forEach { suggestion ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x0FFFFFFF))
                    .border(0.5.dp, Color(0x1F00E5FF), RoundedCornerShape(12.dp))
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                    Text(
                        text = "→",
                        fontSize = 14.sp,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}
