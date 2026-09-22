package com.kumaru.assistant.presentation.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.model.ConversationSession
import com.kumaru.assistant.core.model.MessageRole
import com.kumaru.assistant.presentation.components.GlassCard
import com.kumaru.assistant.presentation.components.KumaruBackground
import com.kumaru.assistant.presentation.components.KumaruMiniOrb
import com.kumaru.assistant.presentation.theme.AccentPinkGradientEnd
import com.kumaru.assistant.presentation.theme.AccentPinkGradientStart
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.GlassSurfaceWhite
import com.kumaru.assistant.presentation.theme.TextOnPink
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary
import com.kumaru.assistant.presentation.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Conversation History Screen for Kumaru V0.2.3.
 * Lists past conversation sessions with real message logs, allowing continuation and deletion.
 */
@Composable
fun HistoryScreen(
    sessions: List<ConversationSession>,
    onSelectSession: (ConversationSession) -> Unit,
    onDeleteSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    KumaruBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = Color(0x12000000))
                            .clip(CircleShape)
                            .background(GlassSurfaceWhite)
                            .border(1.dp, GlassBorderLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // New Chat Button
                Box(
                    modifier = Modifier
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x20E05697))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentPinkGradientStart, AccentPinkGradientEnd)
                            )
                        )
                        .clickable { onNewChat() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "New Chat",
                            tint = TextOnPink,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Chat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextOnPink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (sessions.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        KumaruMiniOrb(size = 48.dp)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Nothing here yet.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your conversation sessions with Kumaru will appear here.",
                            fontSize = 13.5.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        HistorySessionCard(
                            session = session,
                            onClick = { onSelectSession(session) },
                            onDelete = { onDeleteSession(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: ConversationSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = formatSessionTimestamp(session.updatedAt)
    val lastSnippet = session.messages.lastOrNull { it.role == MessageRole.KUMARU }?.text
        ?: session.messages.firstOrNull()?.text
        ?: "Empty conversation"

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        backgroundColor = GlassSurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x12E05697)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = AccentPinkPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = lastSnippet,
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${session.messages.size} msgs",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete conversation",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun formatSessionTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneMin = 60 * 1000L
    val oneHour = 60 * oneMin
    val oneDay = 24 * oneHour

    return when {
        diff < oneMin -> "Just now"
        diff < oneHour -> "${diff / oneMin}m ago"
        diff < oneDay -> "${diff / oneHour}h ago"
        diff < 7 * oneDay -> "${diff / oneDay}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
