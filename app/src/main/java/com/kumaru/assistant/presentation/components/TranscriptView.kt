package com.kumaru.assistant.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.MessageRole
import com.kumaru.assistant.data.prompts.SuggestedPrompt
import com.kumaru.assistant.data.prompts.SuggestedPromptRepository
import com.kumaru.assistant.presentation.theme.AccentPinkGradientEnd
import com.kumaru.assistant.presentation.theme.AccentPinkGradientStart
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.GlassSurfaceTranslucent
import com.kumaru.assistant.presentation.theme.GlassSurfaceWhite
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextOnPink
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary
import com.kumaru.assistant.presentation.theme.TextTertiary

/**
 * Editorial conversation feed for Kumaru V0.2.3.
 * Renders user speech as vibrant soft-pink glass bubbles and Kumaru responses
 * as translucent frosted cards accompanied by the 3D Mini-Orb avatar.
 */
@Composable
fun TranscriptView(
    messages: List<ConversationMessage>,
    onSuggestionClick: (String) -> Unit,
    suggestedPrompts: List<SuggestedPrompt> = SuggestedPromptRepository.getCuratedSuggestions(4),
    onRefreshPrompts: () -> Unit = {},
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
            suggestedPrompts = suggestedPrompts,
            onSuggestionClick = onSuggestionClick,
            onRefreshPrompts = onRefreshPrompts,
            modifier = modifier
        )
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Kumaru 3D Mini-Orb avatar on the left
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, end = 8.dp)
                    .size(26.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = Color(0x15000000))
                    .clip(CircleShape)
                    .background(GlassSurfaceWhite)
                    .border(0.75.dp, GlassBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                KumaruMiniOrb(size = 18.dp)
            }
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            // Message bubble container
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = if (isUser) 3.dp else 2.dp,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 6.dp,
                            bottomEnd = if (isUser) 6.dp else 20.dp
                        ),
                        ambientColor = Color(0x12000000),
                        spotColor = if (isUser) Color(0x28E05697) else Color(0x10000000)
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 6.dp,
                            bottomEnd = if (isUser) 6.dp else 20.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.horizontalGradient(
                                listOf(AccentPinkGradientStart, AccentPinkGradientEnd)
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(GlassSurfaceWhite, GlassSurfaceTranslucent)
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            if (isUser) {
                                listOf(Color(0x80FFFFFF), Color(0x33FFFFFF))
                            } else {
                                listOf(GlassBorderLight, Color(0x40CBD5E1))
                            }
                        ),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 6.dp,
                            bottomEnd = if (isUser) 6.dp else 20.dp
                        )
                    )
                    .padding(horizontal = 15.dp, vertical = 11.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isUser) TextOnPink else TextPrimary
                    )

                    if (!isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Copy button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { clipboardManager.setText(AnnotatedString(message.text)) }
                                    .padding(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            // Vocal indicator
                            Icon(
                                imageVector = Icons.Outlined.VolumeUp,
                                contentDescription = "Voice response",
                                tint = AccentPinkPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTranscriptSuggestions(
    suggestedPrompts: List<SuggestedPrompt>,
    onSuggestionClick: (String) -> Unit,
    onRefreshPrompts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Quick suggestion header with refresh action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "What can we explore?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                color = TextSecondary
            )

            // Refresh Prompt Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onRefreshPrompts() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh Prompts",
                        tint = AccentPinkPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Refresh",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPinkPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = suggestedPrompts,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SuggestedPromptsAnimation"
        ) { prompts ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                prompts.forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x10000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassSurfaceWhite)
                            .border(1.dp, GlassBorderLight, RoundedCornerShape(16.dp))
                            .clickable { onSuggestionClick(prompt.text) }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (prompt.icon != null) {
                                    Icon(
                                        imageVector = prompt.icon,
                                        contentDescription = null,
                                        tint = AccentPinkPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = prompt.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "↗",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPinkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
